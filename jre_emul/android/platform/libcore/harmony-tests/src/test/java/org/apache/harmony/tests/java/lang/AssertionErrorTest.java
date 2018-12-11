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
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.harmony.tests.java.lang;

import junit.framework.TestCase;

public class AssertionErrorTest extends TestCase {

    public void test_Constructor() {
        AssertionError e = new AssertionError();
        assertNull(e.getMessage());
        assertNull(e.getCause());
    }

    public void test_ConstructorObject() {
        Object obj = "toString";
        AssertionError e = new AssertionError(obj);
        assertEquals("toString", e.getMessage());
        assertNull(e.getCause());

        NullPointerException npe = new NullPointerException("null value");
        e = new AssertionError(npe);
        assertEquals(npe.toString(), e.getMessage());
        assertSame(npe, e.getCause());
    }

    public void test_ConstructorBoolean() {
        AssertionError e = new AssertionError(true);
        assertEquals("true", e.getMessage());
        assertNull(e.getCause());
    }

    public void test_ConstructorChar() {
        AssertionError e = new AssertionError('a');
        assertEquals("a", e.getMessage());
        assertNull(e.getCause());
    }

    public void test_ConstructorInt() {
        AssertionError e = new AssertionError(1);
        assertEquals("1", e.getMessage());
        assertNull(e.getCause());
    }

    public void test_ConstructorLong() {
        AssertionError e = new AssertionError(1L);
        assertEquals("1", e.getMessage());
        assertNull(e.getCause());
    }

    public void test_ConstructorFloat() {
        AssertionError e = new AssertionError(1.0F);
        assertEquals("1.0", e.getMessage());
        assertNull(e.getCause());
    }

    public void test_ConstructorDouble() {
        AssertionError e = new AssertionError(1.0D);
        assertEquals("1.0", e.getMessage());
        assertNull(e.getCause());
    }

}
