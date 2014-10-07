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

package tests.api.java.util;

import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.Vector;

public class PropertyResourceBundleTest extends junit.framework.TestCase {

    static PropertyResourceBundle prb;

    /**
     * java.util.PropertyResourceBundle#PropertyResourceBundle(java.io.InputStream)
     */
    public void test_ConstructorLjava_io_InputStream() {
        // Test for method java.util.PropertyResourceBundle(java.io.InputStream)
        assertTrue("Used to test", true);
    }

    /**
     * java.util.PropertyResourceBundle#getKeys()
     */
    @SuppressWarnings("unchecked")
    public void test_getKeys() {
        Enumeration keyEnum = prb.getKeys();
        Vector test = new Vector();
        int keyCount = 0;
        while (keyEnum.hasMoreElements()) {
            test.addElement(keyEnum.nextElement());
            keyCount++;
        }

        assertEquals("Returned the wrong number of keys", 2, keyCount);
        assertTrue("Returned the wrong keys", test.contains("p1")
                && test.contains("p2"));
    }

    /**
     * java.util.PropertyResourceBundle#handleGetObject(java.lang.String)
     */
    public void test_handleGetObjectLjava_lang_String() {
        // Test for method java.lang.Object
        // java.util.PropertyResourceBundle.handleGetObject(java.lang.String)
        try {
            assertTrue("Returned incorrect objects", prb.getObject("p1")
                    .equals("one")
                    && prb.getObject("p2").equals("two"));
        } catch (MissingResourceException e) {
            fail(
                    "Threw MisingResourceException for a key contained in the bundle");
        }
        try {
            prb.getObject("Not in the bundle");
        } catch (MissingResourceException e) {
            return;
        }
        fail(
                "Failed to throw MissingResourceException for object not in the bundle");
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {
        java.io.InputStream propertiesStream = new java.io.ByteArrayInputStream(
                "p1=one\np2=two".getBytes());
        try {
            prb = new PropertyResourceBundle(propertiesStream);
        } catch (java.io.IOException e) {
            fail(
                    "Contruction of PropertyResourceBundle threw IOException");
        }
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
    }
}
