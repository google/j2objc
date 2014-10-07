/* Licensed to the Apache Software Foundation (ASF) under one or more
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

package org.apache.harmony.luni.tests.java.lang;

import junit.framework.TestCase;

public class TypeNotPresentExceptionTest extends TestCase {

    /**
     * @tests java.lang.TypeNotPresentException.TypeNotPresentException(String, Throwable)
     */
    public void test_constructorLjava_lang_StringLjava_lang_Throwable() {
        TypeNotPresentException e = new TypeNotPresentException(null, null);
        assertNotNull(e);
        String m = e.getMessage();
        assertNotNull(m);
        
        e = new TypeNotPresentException(getClass().getName(), null);
        assertNotNull(e);
        m = e.getMessage();
        assertNotNull(m);
        
        NullPointerException npe = new NullPointerException();
        e = new TypeNotPresentException(getClass().getName(), npe);
        assertNotNull(e.getMessage());
        assertSame(npe, e.getCause());
    }

    /**
     * @tests java.lang.TypeNotPresentException.typeName()
     */
    public void test_typeName() {
        TypeNotPresentException e = new TypeNotPresentException(null, null);
        assertNull(e.typeName());
        
        e = new TypeNotPresentException(getClass().getName(), null);
        assertEquals(getClass().getName(), e.typeName());
    }

}
