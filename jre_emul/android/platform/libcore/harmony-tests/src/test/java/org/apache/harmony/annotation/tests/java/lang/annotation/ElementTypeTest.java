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

import java.lang.annotation.ElementType;
import java.util.Arrays;

import junit.framework.TestCase;

/**
 * Test case of java.lang.annotation.ElementType
 */
public class ElementTypeTest extends TestCase {

    /**
     * @throws Exception
     * @tests java.lang.annotation.ElementType#valueOf(String)
     */
    @SuppressWarnings("nls")
    public void test_valueOfLjava_lang_String() throws Exception {
        assertSame(ElementType.ANNOTATION_TYPE, ElementType
                .valueOf("ANNOTATION_TYPE"));
        assertSame(ElementType.CONSTRUCTOR, ElementType.valueOf("CONSTRUCTOR"));
        assertSame(ElementType.FIELD, ElementType.valueOf("FIELD"));
        assertSame(ElementType.LOCAL_VARIABLE, ElementType
                .valueOf("LOCAL_VARIABLE"));
        assertSame(ElementType.METHOD, ElementType.valueOf("METHOD"));
        assertSame(ElementType.PACKAGE, ElementType.valueOf("PACKAGE"));
        assertSame(ElementType.PARAMETER, ElementType.valueOf("PARAMETER"));
        assertSame(ElementType.TYPE, ElementType.valueOf("TYPE"));
        try {
            ElementType.valueOf("OTHER");
            fail("Should throw an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * @throws Exception
     * @tests java.lang.annotation.ElementType#values()
     */
    @SuppressWarnings("nls")
    public void test_values() throws Exception {
        ElementType[] values = ElementType.values();
        assertTrue(values.length > 1);
        Arrays.sort(values);
        assertTrue(Arrays.binarySearch(values, ElementType.METHOD) >= 0);
    }
}
