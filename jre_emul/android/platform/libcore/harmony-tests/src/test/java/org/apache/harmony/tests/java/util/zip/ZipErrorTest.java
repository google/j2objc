/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
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
package org.apache.harmony.tests.java.util.zip;

import java.io.IOException;
import java.util.zip.ZipError;
import org.apache.harmony.testframework.serialization.SerializationTest;
import junit.framework.TestCase;

public class ZipErrorTest extends TestCase {

    /**
     * {@link java.util.zip.ZipError#ZipError(String)}
     */
    public void test_constructor() {
        ZipError error = new ZipError("ZipError");
        assertEquals("ZipError", error.getMessage());
    }

    /**
     * java.util.zip.ZipError#Serialization()
     */
    public void test_serialization() throws Exception {
        ZipError error = new ZipError("serialization test");
        SerializationTest.verifySelf(error);
    }

    /**
     * serialization/deserialization compatibility with RI.
     */
    public void testSerializationCompatibility() throws Exception {
        ZipError error = new ZipError("serialization test");
        SerializationTest.verifyGolden(this, error);
    }

}
