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

import java.lang.annotation.AnnotationFormatError;

import junit.framework.TestCase;

/**
 * Test case of java.lang.annotation.AnnotationFormatError
 */
public class AnnotationFormatErrorTest extends TestCase {
    /**
     * @tests java.lang.annotation.AnnotationFormatError#AnnotationFormatError(String)
     */
    @SuppressWarnings("nls")
    public void test_constructorLjava_lang_String() {
        AnnotationFormatError e = new AnnotationFormatError("some message");
        assertEquals("some message", e.getMessage());
    }

    /**
     * @tests java.lang.annotation.AnnotationFormatError#AnnotationFormatError(Throwable)
     */
    public void test_constructorLjava_lang_Throwable() {
        IllegalArgumentException iae = new IllegalArgumentException();
        AnnotationFormatError e = new AnnotationFormatError(iae);
        assertSame(iae, e.getCause());
    }

    /**
     * @tests java.lang.annotation.AnnotationFormatError#AnnotationFormatError(String,Throwable)
     */
    @SuppressWarnings("nls")
    public void test_constructorLjava_lang_StringLjava_lang_Throwable() {
        IllegalArgumentException iae = new IllegalArgumentException();
        AnnotationFormatError e = new AnnotationFormatError("some message", iae);
        assertEquals("some message", e.getMessage());
        assertSame(iae, e.getCause());
    }
}
