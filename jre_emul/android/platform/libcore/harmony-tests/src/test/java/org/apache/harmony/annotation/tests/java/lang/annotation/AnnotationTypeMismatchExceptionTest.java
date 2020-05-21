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

package org.apache.harmony.annotation.tests.java.lang.annotation;

import java.lang.annotation.AnnotationTypeMismatchException;
import java.lang.reflect.Method;

import junit.framework.TestCase;

/**
 * Test case of java.lang.annotation.AnnotationTypeMismatchException
 */
public class AnnotationTypeMismatchExceptionTest extends TestCase {

    /**
     * @throws ClassNotFoundException 
     * @throws SecurityException 
     * @tests java.lang.annotation.AnnotationTypeMismatchException#AnnotationTypeMismatchException(Method,
     *        String)
     */
    @SuppressWarnings("nls")
    public void test_constructorLjava_lang_reflect_MethodLjava_lang_String() throws SecurityException, ClassNotFoundException {
        Method[] methods = Class.forName("java.lang.String").getMethods();
        Method m = methods[0];
        AnnotationTypeMismatchException e = new AnnotationTypeMismatchException(
                m, "some type");
        assertNotNull("can not instantiate AnnotationTypeMismatchException", e);
        assertSame("wrong method name", m, e.element());
        assertEquals("wrong found type", "some type", e.foundType());
    }
}
