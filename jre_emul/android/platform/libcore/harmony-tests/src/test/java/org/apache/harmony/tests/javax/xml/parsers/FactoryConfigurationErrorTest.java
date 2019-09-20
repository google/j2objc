/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.harmony.tests.javax.xml.parsers;

import javax.xml.parsers.FactoryConfigurationError;

import junit.framework.TestCase;

public class FactoryConfigurationErrorTest extends TestCase {

    public void test_Constructor() {
        FactoryConfigurationError fce = new FactoryConfigurationError();
        assertNull(fce.getMessage());
        assertNull(fce.getLocalizedMessage());
        assertNull(fce.getCause());
    }

    public void test_ConstructorLjava_lang_Exception() {
        Exception e = new Exception();
        // case 1: Try to create FactoryConfigurationError
        // which is based on Exception without parameters.
        FactoryConfigurationError fce = new FactoryConfigurationError(e);
        assertNotNull(fce.getMessage());
        assertNotNull(fce.getLocalizedMessage());
        assertEquals(e.getCause(), fce.getCause());

        // case 2: Try to create FactoryConfigurationError
        // which is based on Exception with String parameter.
        e = new Exception("test message");
        fce = new FactoryConfigurationError(e);
        assertEquals(e.toString(), fce.getMessage());
        assertEquals(e.toString(), fce.getLocalizedMessage());
        assertEquals(e.getCause(), fce.getCause());
    }

    public void test_ConstructorLjava_lang_ExceptionLjava_lang_String() {
        Exception e = new Exception();
        // case 1: Try to create FactoryConfigurationError
        // which is based on Exception without parameters.
        FactoryConfigurationError fce = new FactoryConfigurationError(e, "msg");
        assertNotNull(fce.getMessage());
        assertNotNull(fce.getLocalizedMessage());
        assertEquals(e.getCause(), fce.getCause());

        // case 2: Try to create FactoryConfigurationError
        // which is based on Exception with String parameter.
        e = new Exception("test message");
        fce = new FactoryConfigurationError(e, "msg");
        assertEquals("msg", fce.getMessage());
        assertEquals("msg", fce.getLocalizedMessage());
        assertEquals(e.getCause(), fce.getCause());
    }

    public void test_ConstructorLjava_lang_String() {
        FactoryConfigurationError fce = new FactoryConfigurationError("Oops!");
        assertEquals("Oops!", fce.getMessage());
        assertEquals("Oops!", fce.getLocalizedMessage());
        assertNull(fce.getCause());
    }

    public void test_getException() {
        FactoryConfigurationError fce = new FactoryConfigurationError();
        assertNull(fce.getException());
        fce = new FactoryConfigurationError("test");
        assertNull(fce.getException());
        Exception e = new Exception("msg");
        fce = new FactoryConfigurationError(e);
        assertEquals(e, fce.getException());
        NullPointerException npe = new NullPointerException();
        fce = new FactoryConfigurationError(npe);
        assertEquals(npe, fce.getException());
    }

    public void test_getMessage() {
        assertNull(new FactoryConfigurationError().getMessage());
        assertEquals("msg1",
                new FactoryConfigurationError("msg1").getMessage());
        assertEquals(new Exception("msg2").toString(),
                new FactoryConfigurationError(
                        new Exception("msg2")).getMessage());
        assertEquals(new NullPointerException().toString(),
                new FactoryConfigurationError(
                        new NullPointerException()).getMessage());
    }

}
