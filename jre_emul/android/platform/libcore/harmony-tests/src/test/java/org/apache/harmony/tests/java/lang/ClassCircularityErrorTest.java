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
package org.apache.harmony.tests.java.lang;

import junit.framework.TestCase;

public class ClassCircularityErrorTest extends TestCase {
    // Thrown when a circularity has been detected while initializing a class.

    /**
     * java.lang.ClassCircularityError#ClassCircularityError()
     */
    public void test_ClassCircularityError() {
        new ClassCircularityError();
    }

    /**
     * java.lang.ClassCircularityError#ClassCircularityError(java.lang.String)
     */
    public void test_ClassCircularityError_LString() {
        ClassCircularityError e = new ClassCircularityError(
                "Some Error message");
        assertEquals("Wrong message", "Some Error message", e.getMessage());
    }

}
