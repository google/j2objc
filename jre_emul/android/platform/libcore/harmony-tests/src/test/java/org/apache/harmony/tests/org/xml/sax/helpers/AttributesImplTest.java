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

package org.apache.harmony.tests.org.xml.sax.helpers;

import junit.framework.TestCase;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

public class AttributesImplTest extends TestCase {

    private AttributesImpl empty = new AttributesImpl();

    private AttributesImpl multi = new AttributesImpl();

    @Override
    public void setUp() {
        multi.addAttribute("http://some.uri", "foo", "ns1:foo",
                "string", "abc");
        multi.addAttribute("http://some.uri", "bar", "ns1:bar",
                "string", "xyz");
        multi.addAttribute("http://some.other.uri", "answer", "ns2:answer",
                "int", "42");

        multi.addAttribute("", "gabbaHey", "", "string", "1-2-3-4");
        multi.addAttribute("", "", "gabba:hey", "string", "1-2-3-4");
    }

    public void testAttributesImpl() {
        assertEquals(0, empty.getLength());
        assertEquals(5, multi.getLength());
    }

    public void testAttributesImplAttributes() {
        // Ordinary case
        AttributesImpl ai = new AttributesImpl(empty);
        assertEquals(0, ai.getLength());

        // Another ordinary case
        ai = new AttributesImpl(multi);
        assertEquals(5, ai.getLength());

        // No Attributes
        try {
            ai = new AttributesImpl(null);
            assertEquals(0, ai.getLength());
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    public void testGetLength() {
        AttributesImpl ai = new AttributesImpl(empty);
        assertEquals(0, ai.getLength());

        ai = new AttributesImpl(multi);
        assertEquals(5, ai.getLength());

        for (int i = 4; i >= 0; i--) {
            ai.removeAttribute(i);
            assertEquals(i, ai.getLength());
        }
    }

    public void testGetURI() {
        // Ordinary cases
        assertEquals("http://some.uri", multi.getURI(0));
        assertEquals("http://some.uri", multi.getURI(1));
        assertEquals("http://some.other.uri", multi.getURI(2));
        assertEquals("", multi.getURI(3));
        assertEquals("", multi.getURI(4));

        // Out of range
        assertEquals(null, multi.getURI(-1));
        assertEquals(null, multi.getURI(5));
    }

    public void testGetLocalName() {
        // Ordinary cases
        assertEquals("foo", multi.getLocalName(0));
        assertEquals("bar", multi.getLocalName(1));
        assertEquals("answer", multi.getLocalName(2));
        assertEquals("gabbaHey", multi.getLocalName(3));
        assertEquals("", multi.getLocalName(4));

        // Out of range
        assertEquals(null, multi.getLocalName(-1));
        assertEquals(null, multi.getLocalName(5));
    }

    public void testGetQName() {
        // Ordinary cases
        assertEquals("ns1:foo", multi.getQName(0));
        assertEquals("ns1:bar", multi.getQName(1));
        assertEquals("ns2:answer", multi.getQName(2));
        assertEquals("", multi.getQName(3));
        assertEquals("gabba:hey", multi.getQName(4));

        // Out of range
        assertEquals(null, multi.getQName(-1));
        assertEquals(null, multi.getQName(5));
    }

    public void testGetTypeInt() {
        // Ordinary cases
        assertEquals("string", multi.getType(0));
        assertEquals("string", multi.getType(1));
        assertEquals("int", multi.getType(2));
        assertEquals("string", multi.getType(3));
        assertEquals("string", multi.getType(4));

        // Out of range
        assertEquals(null, multi.getType(-1));
        assertEquals(null, multi.getType(5));
    }

    public void testGetValueInt() {
        // Ordinary cases
        assertEquals("abc", multi.getValue(0));
        assertEquals("xyz", multi.getValue(1));
        assertEquals("42", multi.getValue(2));
        assertEquals("1-2-3-4", multi.getValue(3));
        assertEquals("1-2-3-4", multi.getValue(4));

        // Out of range
        assertEquals(null, multi.getValue(-1));
        assertEquals(null, multi.getValue(5));
    }

    public void testGetIndexStringString() {
        // Ordinary cases
        assertEquals(0, multi.getIndex("http://some.uri", "foo"));
        assertEquals(1, multi.getIndex("http://some.uri", "bar"));
        assertEquals(2, multi.getIndex("http://some.other.uri", "answer"));

        // Not found
        assertEquals(-1, multi.getIndex("john", "doe"));

        // null cases
        assertEquals(-1, multi.getIndex("http://some.uri", null));
        assertEquals(-1, multi.getIndex(null, "foo"));
    }

    public void testGetIndexString() {
        // Ordinary cases
        assertEquals(0, multi.getIndex("ns1:foo"));
        assertEquals(1, multi.getIndex("ns1:bar"));
        assertEquals(2, multi.getIndex("ns2:answer"));
        assertEquals(4, multi.getIndex("gabba:hey"));

        // Not found
        assertEquals(-1, multi.getIndex("john:doe"));

        // null case
        assertEquals(-1, multi.getIndex(null));
    }

    public void testGetTypeStringString() {
        // Ordinary cases
        assertEquals("string", multi.getType("http://some.uri", "foo"));
        assertEquals("string", multi.getType("http://some.uri", "bar"));
        assertEquals("int", multi.getType("http://some.other.uri", "answer"));

        // Not found
        assertEquals(null, multi.getType("john", "doe"));

        // null cases
        assertEquals(null, multi.getType("http://some.uri", null));
        assertEquals(null, multi.getType(null, "foo"));
    }

    public void testGetTypeString() {
        // Ordinary cases
        assertEquals("string", multi.getType("ns1:foo"));
        assertEquals("string", multi.getType("ns1:bar"));
        assertEquals("int", multi.getType("ns2:answer"));
        assertEquals("string", multi.getType("gabba:hey"));

        // Not found
        assertEquals(null, multi.getType("john:doe"));

        // null case
        assertEquals(null, multi.getType(null));
    }

    public void testGetValueStringString() {
        // Ordinary cases
        assertEquals("abc", multi.getValue("http://some.uri", "foo"));
        assertEquals("xyz", multi.getValue("http://some.uri", "bar"));
        assertEquals("42", multi.getValue("http://some.other.uri", "answer"));

        // Not found
        assertEquals(null, multi.getValue("john", "doe"));

        // null cases
        assertEquals(null, multi.getValue("http://some.uri", null));
        assertEquals(null, multi.getValue(null, "foo"));
    }

    public void testGetValueString() {
        // Ordinary cases
        assertEquals("abc", multi.getValue("ns1:foo"));
        assertEquals("xyz", multi.getValue("ns1:bar"));
        assertEquals("42", multi.getValue("ns2:answer"));
        assertEquals("1-2-3-4", multi.getValue("gabba:hey"));

        // Not found
        assertEquals(null, multi.getValue("john:doe"));

        // null case
        assertEquals(null, multi.getValue(null));
    }

    public void testClear() {
        assertEquals(5, multi.getLength());
        multi.clear();
        assertEquals(0, multi.getLength());
    }

    public void testSetAttributes() {
        // Ordinary cases
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute("http://yet.another.uri", "doe", "john:doe",
                "boolean", "false");

        attrs.setAttributes(empty);
        assertEquals(0, attrs.getLength());

        attrs.setAttributes(multi);
        assertEquals(multi.getLength(), attrs.getLength());

        for (int i = 0; i < multi.getLength(); i++) {
            assertEquals(multi.getURI(i), attrs.getURI(i));
            assertEquals(multi.getLocalName(i), attrs.getLocalName(i));
            assertEquals(multi.getQName(i), attrs.getQName(i));
            assertEquals(multi.getType(i), attrs.getType(i));
            assertEquals(multi.getValue(i), attrs.getValue(i));
        }

        // null case
        try {
            attrs.setAttributes(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected, but must be empty now
            assertEquals(0, attrs.getLength());
        }
    }

    public void testAddAttribute() {
        // Ordinary case
        multi.addAttribute("http://yet.another.uri", "doe", "john:doe",
                "boolean", "false");

        assertEquals("http://yet.another.uri", multi.getURI(5));
        assertEquals("doe", multi.getLocalName(5));
        assertEquals("john:doe", multi.getQName(5));
        assertEquals("boolean", multi.getType(5));
        assertEquals("false", multi.getValue(5));

        // Duplicate case
        multi.addAttribute("http://yet.another.uri", "doe", "john:doe",
                "boolean", "false");

        assertEquals("http://yet.another.uri", multi.getURI(6));
        assertEquals("doe", multi.getLocalName(6));
        assertEquals("john:doe", multi.getQName(6));
        assertEquals("boolean", multi.getType(6));
        assertEquals("false", multi.getValue(6));

        // null case
        multi.addAttribute(null, null, null, null, null);
        assertEquals(null, multi.getURI(7));
        assertEquals(null, multi.getLocalName(7));
        assertEquals(null, multi.getQName(7));
        assertEquals(null, multi.getType(7));
        assertEquals(null, multi.getValue(7));
    }

    public void testSetAttribute() {
        // Ordinary case
        multi.setAttribute(0, "http://yet.another.uri", "doe", "john:doe",
                "boolean", "false");
        assertEquals("http://yet.another.uri", multi.getURI(0));
        assertEquals("doe", multi.getLocalName(0));
        assertEquals("john:doe", multi.getQName(0));
        assertEquals("boolean", multi.getType(0));
        assertEquals("false", multi.getValue(0));

        // null case
        multi.setAttribute(1, null, null, null, null, null);
        assertEquals(null, multi.getURI(1));
        assertEquals(null, multi.getLocalName(1));
        assertEquals(null, multi.getQName(1));
        assertEquals(null, multi.getType(1));
        assertEquals(null, multi.getValue(1));

        // Out of range
        try {
            multi.setAttribute(-1, "http://yet.another.uri", "doe", "john:doe",
                    "boolean", "false");
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Expected
        }

        try {
            multi.setAttribute(5, "http://yet.another.uri", "doe", "john:doe",
                    "boolean", "false");
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Expected
        }
    }

    public void testRemoveAttribute() {
        // Ordinary case
        multi.removeAttribute(0);
        assertEquals("http://some.uri", multi.getURI(0));
        assertEquals("bar", multi.getLocalName(0));
        assertEquals("ns1:bar", multi.getQName(0));
        assertEquals("string", multi.getType(0));
        assertEquals("xyz", multi.getValue(0));

        // Out of range
        try {
            multi.removeAttribute(-1);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Expected
        }

        try {
            multi.removeAttribute(4);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Expected
        }
    }

    public void testSetURI() {
        // Ordinary case
        multi.setURI(0, "http://yet.another.uri");
        assertEquals("http://yet.another.uri", multi.getURI(0));

        // null case
        multi.setURI(1, null);
        assertEquals(null, multi.getURI(1));

        // Out of range
        try {
            multi.setURI(-1, "http://yet.another.uri");
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Expected
        }

        try {
            multi.setURI(5, "http://yet.another.uri");
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Expected
        }
    }

    public void testSetLocalName() {
        // Ordinary case
        multi.setLocalName(0, "john");
        assertEquals("john", multi.getLocalName(0));

        // null case
        multi.setLocalName(1, null);
        assertEquals(null, multi.getLocalName(1));

        // Out of range
        try {
            multi.setLocalName(-1, "john");
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Expected
        }

        try {
            multi.setLocalName(5, "john");
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Expected
        }
    }

    public void testSetQName() {
        // Ordinary case
        multi.setQName(0, "john:doe");
        assertEquals("john:doe", multi.getQName(0));

        // null case
        multi.setQName(1, null);
        assertEquals(null, multi.getQName(1));

        // Out of range
        try {
            multi.setQName(-1, "john:doe");
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Expected
        }

        try {
            multi.setQName(5, "john:doe");
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Expected
        }
    }

    public void testSetType() {
        // Ordinary case
        multi.setType(0, "float");
        assertEquals("float", multi.getType(0));

        // null case
        multi.setType(1, null);
        assertEquals(null, multi.getType(1));

        // Out of range
        try {
            multi.setType(-1, "float");
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Expected
        }

        try {
            multi.setType(5, "float");
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Expected
        }
    }

    public void testSetValue() {
        // Ordinary case
        multi.setValue(0, "too much");
        assertEquals("too much", multi.getValue(0));

        // null case
        multi.setValue(1, null);
        assertEquals(null, multi.getValue(1));

        // Out of range
        try {
            multi.setValue(-1, "too much");
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Expected
        }

        try {
            multi.setValue(5, "too much");
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Expected
        }
    }

}
