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

package org.apache.harmony.luni.tests.java.lang;

import java.io.IOException;

import junit.framework.TestCase;

public class ClassNotFoundExceptionTest extends TestCase {
    /**
     * Thrown when an application tries to load in a class through its string
     * name using the forName method in class Class.
     */

	/**
	 * @tests java.lang.ClassNotFoundException#ClassNotFoundException()
	 */
	public void test_Constructor() {
        ClassNotFoundException e = new ClassNotFoundException();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
	}

	/**
	 * @tests java.lang.ClassNotFoundException#ClassNotFoundException(java.lang.String)
	 */
	public void test_ConstructorLjava_lang_String() {
        ClassNotFoundException e = new ClassNotFoundException("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
	}
	
    /**
     * @tests java.lang.ClassNotFoundException#ClassNotFoundException(java.lang.String, java.lang.Throwable)
     */
    public void test_ClassNotFoundException_LString_LThrowable() {
        IOException in = new IOException();
        ClassNotFoundException e = new ClassNotFoundException("SomeMessage", in);
        assertEquals("Wrong Exception", in, e.getException());
        assertEquals("Wrong message", "SomeMessage", e.getMessage());
        assertEquals("Wrong cause", in, e.getCause());
    }

}
