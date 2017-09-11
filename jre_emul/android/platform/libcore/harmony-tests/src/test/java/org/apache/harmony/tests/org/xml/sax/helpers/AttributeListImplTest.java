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

import org.xml.sax.AttributeList;
import org.xml.sax.helpers.AttributeListImpl;

@SuppressWarnings("deprecation")
public class AttributeListImplTest extends TestCase {

    private AttributeListImpl empty = new AttributeListImpl();

    private AttributeListImpl multi = new AttributeListImpl();

    @Override
    public void setUp() {
        multi.addAttribute("foo", "string", "abc");
        multi.addAttribute("bar", "string", "xyz");
        multi.addAttribute("answer", "int", "42");
    }

    public void testAttributeListImpl() {
        assertEquals(0, empty.getLength());
        assertEquals(3, multi.getLength());
    }

    public void testAttributeListImplAttributeList() {
        // Ordinary case
        AttributeListImpl ai = new AttributeListImpl(empty);
        assertEquals(0, ai.getLength());

        // Another ordinary case
        ai = new AttributeListImpl(multi);
        assertEquals(3, ai.getLength());

        // No Attributes
        try {
            ai = new AttributeListImpl(null);
            assertEquals(0, ai.getLength());
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    public void testSetAttributeList() {
        // Ordinary cases
        AttributeListImpl attrs = new AttributeListImpl();
        attrs.addAttribute("doe", "boolean", "false");

        attrs.setAttributeList(empty);
        assertEquals(0, attrs.getLength());

        attrs.setAttributeList(multi);
        assertEquals(multi.getLength(), attrs.getLength());

        for (int i = 0; i < multi.getLength(); i++) {
            assertEquals(multi.getName(i), attrs.getName(i));
            assertEquals(multi.getType(i), attrs.getType(i));
            assertEquals(multi.getValue(i), attrs.getValue(i));
        }

        // null case
        try {
            attrs.setAttributeList(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected, must still have old elements
            assertEquals(3, attrs.getLength());
        }
    }

    public void testAddAttribute() {
        // Ordinary case
        multi.addAttribute("doe", "boolean", "false");

        assertEquals("doe", multi.getName(3));
        assertEquals("boolean", multi.getType(3));
        assertEquals("false", multi.getValue(3));

        // Duplicate case
        multi.addAttribute("doe", "boolean", "false");

        assertEquals("doe", multi.getName(4));
        assertEquals("boolean", multi.getType(4));
        assertEquals("false", multi.getValue(4));

        // null case
        multi.addAttribute(null, null, null);
        assertEquals(null, multi.getName(5));
        assertEquals(null, multi.getType(5));
        assertEquals(null, multi.getValue(5));
    }

    public void testRemoveAttribute() {
        // Ordinary case
        multi.removeAttribute("foo");
        assertEquals("bar", multi.getName(0));
        assertEquals("string", multi.getType(0));
        assertEquals("xyz", multi.getValue(0));

        // Unknown attribute
        multi.removeAttribute("john");
        assertEquals(2, multi.getLength());

        // null case
        multi.removeAttribute(null);
        assertEquals(2, multi.getLength());
    }

    public void testClear() {
        assertEquals(3, multi.getLength());
        multi.clear();
        assertEquals(0, multi.getLength());
    }

    public void testGetLength() {
        AttributeListImpl ai = new AttributeListImpl(empty);
        assertEquals(0, ai.getLength());

        ai = new AttributeListImpl(multi);
        assertEquals(3, ai.getLength());

        for (int i = 2; i >= 0; i--) {
            ai.removeAttribute(ai.getName(i));
            assertEquals(i, ai.getLength());
        }
    }

    public void testGetName() {
        // Ordinary cases
        assertEquals("foo", multi.getName(0));
        assertEquals("bar", multi.getName(1));
        assertEquals("answer", multi.getName(2));

        // Out of range
        assertEquals(null, multi.getName(-1));
        assertEquals(null, multi.getName(3));
    }

    public void testGetTypeInt() {
        // Ordinary cases
        assertEquals("string", multi.getType(0));
        assertEquals("string", multi.getType(1));
        assertEquals("int", multi.getType(2));

        // Out of range
        assertEquals(null, multi.getType(-1));
        assertEquals(null, multi.getType(3));
    }

    public void testGetValueInt() {
        // Ordinary cases
        assertEquals("abc", multi.getValue(0));
        assertEquals("xyz", multi.getValue(1));
        assertEquals("42", multi.getValue(2));

        // Out of range
        assertEquals(null, multi.getValue(-1));
        assertEquals(null, multi.getValue(5));
    }

    public void testGetTypeString() {
        // Ordinary cases
        assertEquals("string", multi.getType("foo"));
        assertEquals("string", multi.getType("bar"));
        assertEquals("int", multi.getType("answer"));

        // Not found
        assertEquals(null, multi.getType("john"));

        // null case
        assertEquals(null, multi.getType(null));
    }

    public void testGetValueString() {
        // Ordinary cases
        assertEquals("abc", multi.getValue("foo"));
        assertEquals("xyz", multi.getValue("bar"));
        assertEquals("42", multi.getValue("answer"));

        // Not found
        assertEquals(null, multi.getValue("john"));

        // null case
        assertEquals(null, multi.getValue(null));
    }

}
