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

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

public class ParserConfigurationExceptionTest extends TestCase{

    public void test_Constructor() {
        ParserConfigurationException pce = new ParserConfigurationException();
        assertNull(pce.getMessage());
        assertNull(pce.getLocalizedMessage());
        assertNull(pce.getCause());
    }

    public void test_ConstructorLjava_lang_String() {
        ParserConfigurationException pce =
            new ParserConfigurationException("Oops!");
        assertEquals("Oops!", pce.getMessage());
        assertEquals("Oops!", pce.getLocalizedMessage());
        assertNull(pce.getCause());
    }

}
