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

package org.apache.harmony.tests.org.xml.sax.ext;

import junit.framework.TestCase;

import org.xml.sax.Attributes;
import org.xml.sax.ext.Attributes2Impl;
import org.xml.sax.helpers.AttributesImpl;

public class Attributes2ImplTest extends TestCase {

    // Note: The original SAX2 implementation of Attributes2Impl is
    // severely broken. Thus all of these tests will probably fail
    // unless the Android implementation of the class gets fixed.

    private Attributes2Impl empty = new Attributes2Impl();

    private Attributes2Impl multi = new Attributes2Impl();

    private Attributes2Impl cdata = new Attributes2Impl();

    @Override
    public void setUp() {
        multi.addAttribute("http://some.uri", "foo", "ns1:foo",
                "string", "abc");
        multi.addAttribute("http://some.uri", "bar", "ns1:bar",
                "string", "xyz");
        multi.addAttribute("http://some.other.uri", "answer", "ns2:answer",
                "int", "42");
        multi.addAttribute("http://yet.another.uri", "gabba", "ns3:gabba",
                "string", "gabba");

        multi.setDeclared(0, false);
        multi.setSpecified(0, false);

        multi.setDeclared(1, true);
        multi.setSpecified(1, false);

        multi.setDeclared(2, false);
        multi.setSpecified(2, true);

        multi.setDeclared(3, true);
        multi.setSpecified(3, true);

        cdata.addAttribute("http://yet.another.uri", "hey", "ns3:hey",
                "CDATA", "hey");
    }

    public void testSetAttributes() {
        // Ordinary case with Attributes2Impl
        Attributes2Impl attrs = new Attributes2Impl();
        attrs.addAttribute("", "", "john", "string", "doe");

        attrs.setAttributes(empty);
        assertEquals(0, attrs.getLength());

        attrs.setAttributes(multi);
        for (int i = 0; i < multi.getLength(); i++) {
            assertEquals(multi.getURI(i), attrs.getURI(i));
            assertEquals(multi.getLocalName(i), attrs.getLocalName(i));
            assertEquals(multi.getQName(i), attrs.getQName(i));
            assertEquals(multi.getType(i), attrs.getType(i));
            assertEquals(multi.getValue(i), attrs.getValue(i));
            assertEquals(multi.isDeclared(i), attrs.isDeclared(i));
            assertEquals(multi.isSpecified(i), attrs.isSpecified(i));
        }

        attrs.setAttributes(empty);
        assertEquals(0, attrs.getLength());

        // Ordinary case with AttributesImpl
        attrs.setAttributes(new AttributesImpl(multi));
        assertEquals(multi.getLength(), attrs.getLength());

        for (int i = 0; i < multi.getLength(); i++) {
            assertEquals(multi.getURI(i), attrs.getURI(i));
            assertEquals(multi.getLocalName(i), attrs.getLocalName(i));
            assertEquals(multi.getQName(i), attrs.getQName(i));
            assertEquals(multi.getType(i), attrs.getType(i));
            assertEquals(multi.getValue(i), attrs.getValue(i));
            assertEquals(true, attrs.isDeclared(i));
            assertEquals(true, attrs.isSpecified(i));
        }

        // Special case with CDATA
        attrs.setAttributes(new AttributesImpl(cdata));
        assertEquals(1, attrs.getLength());
        assertEquals(false, attrs.isDeclared(0));
        assertEquals(true, attrs.isSpecified(0));

        // null case
        try {
            attrs.setAttributes(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    public void testAddAttribute() {
        Attributes2Impl attrs = new Attributes2Impl();

        // Ordinary case
        attrs.addAttribute("http://yet.another.uri", "doe", "john:doe",
                "string", "abc");

        assertEquals(1, attrs.getLength());

        assertEquals("http://yet.another.uri", attrs.getURI(0));
        assertEquals("doe", attrs.getLocalName(0));
        assertEquals("john:doe", attrs.getQName(0));
        assertEquals("string", attrs.getType(0));
        assertEquals("abc", attrs.getValue(0));

        assertEquals(true, attrs.isDeclared(0));
        assertEquals(true, attrs.isSpecified(0));

        // CDATA case
        attrs.addAttribute("http://yet.another.uri", "doe", "jane:doe",
                "CDATA", "abc");

        assertEquals(2, attrs.getLength());

        assertEquals("http://yet.another.uri", attrs.getURI(1));
        assertEquals("doe", attrs.getLocalName(1));
        assertEquals("jane:doe", attrs.getQName(1));
        assertEquals("CDATA", attrs.getType(1));
        assertEquals("abc", attrs.getValue(1));

        assertEquals(false, attrs.isDeclared(1));
        assertEquals(true, attrs.isSpecified(1));
    }

    public void testRemoveAttribute() {
        Attributes2Impl attrs = new Attributes2Impl(multi);

        // Ordinary case
        attrs.removeAttribute(1);

        assertEquals(3, attrs.getLength());

        assertEquals(multi.getURI(0), attrs.getURI(0));
        assertEquals(multi.getLocalName(0), attrs.getLocalName(0));
        assertEquals(multi.getQName(0), attrs.getQName(0));
        assertEquals(multi.getType(0), attrs.getType(0));
        assertEquals(multi.getValue(0), attrs.getValue(0));
        assertEquals(multi.isDeclared(0), attrs.isDeclared(0));
        assertEquals(multi.isSpecified(0), attrs.isSpecified(0));

        assertEquals(multi.getURI(2), attrs.getURI(1));
        assertEquals(multi.getLocalName(2), attrs.getLocalName(1));
        assertEquals(multi.getQName(2), attrs.getQName(1));
        assertEquals(multi.getType(2), attrs.getType(1));
        assertEquals(multi.getValue(2), attrs.getValue(1));
        assertEquals(multi.isDeclared(2), attrs.isDeclared(1));
        assertEquals(multi.isSpecified(2), attrs.isSpecified(1));

        // Out of range
        try {
            attrs.removeAttribute(-1);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Expected
        }

        try {
            attrs.removeAttribute(3);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Expected
        }
    }

    public void testAttributes2Impl() {
        assertEquals(0, empty.getLength());
    }

    public void testAttributes2ImplAttributes() {
        // Ordinary case with Attributes2Impl
        Attributes2Impl attrs = new Attributes2Impl(multi);
        assertEquals(multi.getLength(), attrs.getLength());

        for (int i = 0; i < multi.getLength(); i++) {
            assertEquals(multi.getURI(i), attrs.getURI(i));
            assertEquals(multi.getLocalName(i), attrs.getLocalName(i));
            assertEquals(multi.getQName(i), attrs.getQName(i));
            assertEquals(multi.getType(i), attrs.getType(i));
            assertEquals(multi.getValue(i), attrs.getValue(i));
            assertEquals(multi.isDeclared(i), attrs.isDeclared(i));
            assertEquals(multi.isSpecified(i), attrs.isSpecified(i));
        }

        attrs = new Attributes2Impl(empty);
        assertEquals(0, attrs.getLength());

        // Ordinary case with AttributesImpl
        attrs = new Attributes2Impl(new AttributesImpl(multi));
        assertEquals(multi.getLength(), attrs.getLength());

        for (int i = 0; i < multi.getLength(); i++) {
            assertEquals(multi.getURI(i), attrs.getURI(i));
            assertEquals(multi.getLocalName(i), attrs.getLocalName(i));
            assertEquals(multi.getQName(i), attrs.getQName(i));
            assertEquals(multi.getType(i), attrs.getType(i));
            assertEquals(multi.getValue(i), attrs.getValue(i));
            assertEquals(true, attrs.isDeclared(i));
            assertEquals(true, attrs.isSpecified(i));
        }

        // Special case with CDATA
        attrs = new Attributes2Impl(new AttributesImpl(cdata));
        assertEquals(1, attrs.getLength());
        assertEquals(false, attrs.isDeclared(0));
        assertEquals(true, attrs.isSpecified(0));

        // null case
        try {
            attrs = new Attributes2Impl(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    public void testIsDeclaredInt() {
        // Ordinary cases
        assertEquals(false, multi.isDeclared(0));
        assertEquals(true, multi.isDeclared(1));

        // Out of range
        try {
            multi.isDeclared(-1);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Expected
        }

        try {
            multi.isDeclared(4);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Expected
        }
    }

    public void testIsDeclaredStringString() {
        // Ordinary cases
        assertEquals(false, multi.isDeclared("http://some.uri", "foo"));
        assertEquals(true, multi.isDeclared("http://some.uri", "bar"));

        // Not found
        try {
            assertFalse(multi.isDeclared("not", "found"));
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    public void testIsDeclaredString() {
        // Ordinary cases
        assertEquals(false, multi.isDeclared("ns1:foo"));
        assertEquals(true, multi.isDeclared("ns1:bar"));

        // Not found
        try {
            assertFalse(multi.isDeclared("notfound"));
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    public void testIsSpecifiedInt() {
        // Ordinary cases
        assertEquals(false, multi.isSpecified(1));
        assertEquals(true, multi.isSpecified(2));

        // Out of range
        try {
            multi.isSpecified(-1);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Expected
        }

        try {
            multi.isSpecified(4);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Expected
        }
    }

    public void testIsSpecifiedStringString() {
        // Ordinary cases
        assertEquals(false, multi.isSpecified("http://some.uri", "bar"));
        assertEquals(true, multi.isSpecified("http://some.other.uri", "answer"));

        // Not found
        try {
            assertFalse(multi.isSpecified("not", "found"));
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    public void testIsSpecifiedString() {
        // Ordinary cases
        assertEquals(false, multi.isSpecified("ns1:bar"));
        assertEquals(true, multi.isSpecified("ns2:answer"));

        // Not found
        try {
            assertFalse(multi.isSpecified("notfound"));
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    public void testSetDeclared() {
        // Ordinary cases
        multi.setSpecified(0, false);
        assertEquals(false, multi.isSpecified(0));

        multi.setSpecified(0, true);
        assertEquals(true, multi.isSpecified(0));

        multi.setSpecified(0, false);
        assertEquals(false, multi.isSpecified(0));

        // Out of range
        try {
            multi.setSpecified(-1, true);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Expected
        }

        try {
            multi.setSpecified(5, true);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Expected
        }
    }

    public void testSetSpecified() {
        // Ordinary cases
        multi.setSpecified(0, false);
        assertEquals(false, multi.isSpecified(0));

        multi.setSpecified(0, true);
        assertEquals(true, multi.isSpecified(0));

        multi.setSpecified(0, false);
        assertEquals(false, multi.isSpecified(0));

        // Out of range
        try {
            multi.setSpecified(-1, true);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Expected
        }

        try {
            multi.setSpecified(5, true);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Expected
        }
    }

}
