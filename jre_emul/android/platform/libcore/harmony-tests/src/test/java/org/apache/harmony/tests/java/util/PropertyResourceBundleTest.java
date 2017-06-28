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

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Vector;

public class PropertyResourceBundleTest extends junit.framework.TestCase {

    static PropertyResourceBundle prb;

    /**
     * @throws IOException
     * java.util.PropertyResourceBundle#PropertyResourceBundle(java.io.InputStream)
     */
    @SuppressWarnings("nls")
    public void test_ConstructorLjava_io_InputStream() throws IOException {
        InputStream propertiesStream = new ByteArrayInputStream(
                "p1=one\ncharset=iso-8859-1".getBytes("ISO-8859-1"));
        prb = new PropertyResourceBundle(propertiesStream);
        assertEquals(2, prb.keySet().size());
        assertEquals("one", prb.getString("p1"));
        assertEquals("iso-8859-1", prb.getString("charset"));

        propertiesStream = new ByteArrayInputStream("p1=one\ncharset=UTF-8"
                .getBytes("UTF-8"));
        prb = new PropertyResourceBundle(propertiesStream);
        assertEquals(2, prb.keySet().size());
        assertEquals("UTF-8", prb.getString("charset"));

        try {
            new PropertyResourceBundle((InputStream) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @throws IOException
     * {@link java.util.PropertyResourceBundle#PropertyResourceBundle(java.io.Reader)}
     * @since 1.6
     */
    @SuppressWarnings("nls")
    public void test_ConstructorLjava_io_Reader() throws IOException {
        Charset charset = Charset.forName("ISO-8859-1");
        String content = "p1=one\nfeature=good_feature";
        CharBuffer cbuffer = charset.decode(ByteBuffer.wrap(content
                .getBytes("ISO-8859-1")));
        char[] chars = new char[cbuffer.limit()];
        cbuffer.get(chars);

        prb = new PropertyResourceBundle(new CharArrayReader(chars));
        assertEquals(2, prb.keySet().size());
        assertEquals("one", prb.getString("p1"));
        assertEquals("good_feature", prb.getString("feature"));

        charset = Charset.forName("UTF-8");
        cbuffer = charset.decode(ByteBuffer.wrap(content.getBytes("UTF-8")));
        chars = new char[cbuffer.limit()];
        cbuffer.get(chars);

        prb = new PropertyResourceBundle(new CharArrayReader(chars));
        assertEquals(2, prb.keySet().size());
        assertEquals("one", prb.getString("p1"));
        assertEquals("good_feature", prb.getString("feature"));

        try {
            new PropertyResourceBundle((Reader) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * java.util.PropertyResourceBundle#getKeys()
     */
    public void test_getKeys() {
        Enumeration keyEnum = prb.getKeys();
        Vector<Object> test = new Vector<Object>();
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
     *
     * @throws UnsupportedEncodingException
     */
    protected void setUp() throws UnsupportedEncodingException {
        InputStream propertiesStream = new ByteArrayInputStream(
                "p1=one\np2=two".getBytes("ISO-8859-1"));
        try {
            prb = new PropertyResourceBundle(propertiesStream);
        } catch (java.io.IOException e) {
            fail(
                    "Construction of PropertyResourceBundle threw IOException");
        }
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
    }

    /**
     * {@link java.util.PropertyResourceBundle#Enumeration}
     */
    public void test_access$0_Enumeration() throws IOException {
        class MockResourceBundle extends PropertyResourceBundle {
            MockResourceBundle(java.io.InputStream stream) throws IOException {
                super(stream);
            }

            @Override
            protected void setParent(ResourceBundle bundle) {
                super.setParent(bundle);
            }
        }

        java.io.InputStream localStream = new java.io.ByteArrayInputStream(
                "p3=three\np4=four".getBytes());
        MockResourceBundle localPrb = new MockResourceBundle(localStream);
        localPrb.setParent(prb);
        Enumeration<String> keys = localPrb.getKeys();
        Vector<String> contents = new Vector<String>();
        while (keys.hasMoreElements()) {
            contents.add(keys.nextElement());
        }

        assertEquals("did not get the right number of properties", 4, contents
                .size());
        assertTrue("did not get the parent property p1", contents
                .contains("p1"));
        assertTrue("did not get the parent property p2", contents
                .contains("p2"));
        assertTrue("did not get the local property p3", contents.contains("p3"));
        assertTrue("did not get the local property p4", contents.contains("p4"));
    }
}
