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

package org.apache.harmony.tests.java.nio.charset;

import java.io.Serializable;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.MalformedInputException;

import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

/**
 * Test class MalformedInputException.
 */
public class MalformedInputExceptionTest extends TestCase {

	public void testConstructor() {
		MalformedInputException ex = new MalformedInputException(3);
		assertTrue(ex instanceof CharacterCodingException);
		assertNull(ex.getCause());
		assertEquals(ex.getInputLength(), 3);
		assertTrue(ex.getMessage().indexOf("3") != -1);

		ex = new MalformedInputException(-3);
		assertNull(ex.getCause());
		assertEquals(ex.getInputLength(), -3);
		assertTrue(ex.getMessage().indexOf("-3") != -1);

		ex = new MalformedInputException(0);
		assertNull(ex.getCause());
		assertEquals(ex.getInputLength(), 0);
		assertTrue(ex.getMessage().indexOf("0") != -1);
	}

    // comparator for MalformedInputException objects
    private static final SerializableAssert COMPARATOR = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {

            // do common checks for all throwable objects
            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
                    deserialized);

            MalformedInputException initEx = (MalformedInputException) initial;
            MalformedInputException desrEx = (MalformedInputException) deserialized;

            assertEquals("InputLength", initEx.getInputLength(), desrEx
                    .getInputLength());
        }
    };

    /**
     * @tests serialization/deserialization compatibility.
     */
    public void testSerializationSelf() throws Exception {

        SerializationTest.verifySelf(new MalformedInputException(11),
                COMPARATOR);
    }

    /**
     * @tests serialization/deserialization compatibility with RI.
     */
    public void testSerializationCompatibility() throws Exception {

        SerializationTest.verifyGolden(this, new MalformedInputException(11),
                COMPARATOR);
    }
}
