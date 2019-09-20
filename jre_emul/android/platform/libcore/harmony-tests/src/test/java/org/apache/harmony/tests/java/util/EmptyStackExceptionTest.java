/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.harmony.tests.java.util;

import java.util.EmptyStackException;
import java.util.Stack;

public class EmptyStackExceptionTest extends junit.framework.TestCase {

    Object[] objArray = new Object[10];
    Stack s;

    /**
     * java.util.EmptyStackException#EmptyStackException()
     */
    public void test_Constructor() {
        // Test for method java.util.EmptyStackException()
        try {
            for (int counter = 0; counter < objArray.length + 1; counter++)
                s.pop();
        } catch (EmptyStackException e) {
            return;
        }
        fail("Expected EmptyStackException not thrown");
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {
        for (int counter = 0; counter < objArray.length; counter++) {
            objArray[counter] = new Integer(counter);
        }

        s = new Stack();
        for (int counter = 0; counter < objArray.length; counter++) {
            s.push(objArray[counter]);
        }
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
        objArray = null;
        s = null;
    }
}
