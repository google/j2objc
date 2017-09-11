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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.Enumeration;

import junit.framework.TestCase;

import org.xml.sax.helpers.NamespaceSupport;

public class NamespaceSupportTest extends TestCase {

    final static String defaultUri = "http://www.android.com";
    final static String marketUri = "http://www.android.com/market";

    NamespaceSupport ns;
    ArrayList<String> expected;

    @Override
    public void setUp() {
        expected = new ArrayList<String>();
        expected.add("ak");
        expected.add("bk");

        ns = new NamespaceSupport();
        ns.pushContext();

        ns.declarePrefix("ak", marketUri);
        ns.declarePrefix("bk", marketUri);
        ns.declarePrefix("", defaultUri);
    }

    @SuppressWarnings("unchecked")
    public void testConstructor() {
        String prefix;
        boolean xmlPrefixExists = false;

        ns = new NamespaceSupport();
        Enumeration<String> prefixes = ns.getDeclaredPrefixes();

        while (prefixes.hasMoreElements()) {
            prefix = prefixes.nextElement();
            if (prefix.equals("xml")) xmlPrefixExists = true;
        }

        assertTrue("Test 1: xml prefix does not exist.", xmlPrefixExists);

        // Check that only one context has been created by the constructor.
        try {
            ns.popContext();
            fail("Test 2: EmptyStackException expected.");
        } catch (EmptyStackException e) {
            // Expected.
        }
    }

    public void testPush_PopContext() {
        int count;

        ns = new NamespaceSupport();
        count = countPrefixes();

        ns.pushContext();
        ns.declarePrefix("dc", "http://www.purl.org/dc#");
        assertEquals("Test 1: Incorrect prefix count;",
                count + 1, countPrefixes());

        ns.popContext();
        assertEquals("Test 2: Incorrect prefix count;",
                count, countPrefixes());

        // Check that only one context has been created by pushContext().
        try {
            ns.popContext();
            fail("Test 3: EmptyStackException expected.");
        } catch (EmptyStackException e) {
            // Expected.
        }
    }

    public void testReset() {
        int count;

        ns = new NamespaceSupport();
        count = countPrefixes();

        ns.pushContext();
        ns.declarePrefix("dc", "http://www.purl.org/dc#");

        assertEquals("Test 1: Incorrect prefix count;",
                count + 1, countPrefixes());

        ns.reset();
        assertEquals("Test 2: Incorrect prefix count;",
                count, countPrefixes());

        // Check that only one context has been created by reset().
        try {
            ns.popContext();
            fail("Test 3: EmptyStackException expected.");
        } catch (EmptyStackException e) {
            // Expected.
        }
    }

    public void testDeclare_GetPrefix() {
        ns.pushContext();

        // Part 1: Check that xml and xmlns are not accepted as prefixes.
        assertFalse("Test 1: Invalid prefix accepted.",
                ns.declarePrefix("xml", marketUri));

        assertFalse("Test 2: Invalid prefix accepted.",
                ns.declarePrefix("xmlns", marketUri));

        // Part 2: Check that declarePrefix and getPrefix work for valid
        // prefixes.
        assertTrue("Test 3: Valid prefix not accepted.",
                ns.declarePrefix("ak", marketUri));

        assertTrue("Test 4: Incorrect prefix returned.",
                ns.getPrefix(marketUri).equals("ak"));

        assertTrue("Test 5: Valid prefix not accepted.",
                ns.declarePrefix("bk", marketUri));

        assertTrue("Test 6: Incorrect prefix returned.",
                expected.contains(ns.getPrefix(marketUri)));

        assertTrue("Test 7: Valid prefix not accepted.",
                ns.declarePrefix("", defaultUri));

        // Part 3: Negative Tests for getPrefix.
        assertNull("Test 8: Non-null value returned for the URI that is " +
                "assigned to the default namespace.",
                ns.getPrefix(defaultUri));

        assertNull("Test 9: Non-null value returned for an unassigned URI.",
                ns.getPrefix(defaultUri + "/42"));
    }

    @SuppressWarnings("unchecked")
    public void testGetPrefixesLjava_lang_String() {
        ArrayList<String> prefixes;

        prefixes = Collections.list(ns.getPrefixes(marketUri));
        assertTrue("Test 1: Incorrect set of prefixes returned.",
                expected.containsAll(prefixes) && prefixes.containsAll(expected));

        prefixes = Collections.list(ns.getPrefixes(defaultUri));
        assertTrue("Test 2: Default namespace prefix should not be returned.",
                prefixes.size() == 0);

        prefixes = Collections.list(ns.getPrefixes(NamespaceSupport.XMLNS));
        assertTrue("Test 3: xml prefix is missing.",
                prefixes.contains("xml") && prefixes.size() == 1);

        prefixes = Collections.list(ns.getPrefixes(defaultUri + "/42"));
        assertTrue("Test 4: Non-empty enumeration returned for an unassigned URI.",
                prefixes.size() == 0);
    }

    @SuppressWarnings("unchecked")
    public void testGetPrefixes() {
        ArrayList<String> prefixes;

        expected.add("xml");

        prefixes = Collections.list(ns.getPrefixes());
        assertTrue("Test 1: Incorrect set of prefixes returned.",
                expected.containsAll(prefixes) && prefixes.containsAll(expected));
    }

    @SuppressWarnings("unchecked")
    public void testGetDeclaredPrefixes() {
        ArrayList<String> prefixes;

        expected.add("");

        prefixes = Collections.list(ns.getDeclaredPrefixes());
        assertTrue("Test 1: Incorrect set of prefixes returned.",
                expected.containsAll(prefixes) && prefixes.containsAll(expected));
    }

    public void testGetUri() {
        assertEquals("Test 1: Incorrect URI returned;",
                marketUri, ns.getURI("bk"));
        assertEquals("Test 2: Incorrect URI returned;",
                defaultUri, ns.getURI(""));
        assertNull("Test 3: Null expected for not-existing prefix.",
                ns.getURI("ck"));

        ns.popContext();
        assertNull("Test 4: Null expected for not-existing prefix.",
                ns.getURI("bk"));
        assertEquals("Test 5: Incorrect URI returned;",
                NamespaceSupport.XMLNS, ns.getURI("xml"));
    }

    public void testNamespaceDeclUris() {

        assertFalse("Test 1: Incorrect default value returned by isNamespaceDeclUris().",
                ns.isNamespaceDeclUris());

        try {
            ns.setNamespaceDeclUris(true);
            fail("Test 2: IllegalStateException expected since a context has already been pushed in setUp().");
        } catch (IllegalStateException e) {
            // Expected.
        }

        ns = new NamespaceSupport();
        ns.setNamespaceDeclUris(true);
        assertTrue("Test 3: Incorrect value returned by isNamespaceDeclUris().",
                ns.isNamespaceDeclUris());

        ns.setNamespaceDeclUris(false);
        assertFalse("Test 4: Incorrect value returned by isNamespaceDeclUris().",
                ns.isNamespaceDeclUris());
    }

    public void testProcessName_Element() {
        String[] parts = new String[3];

        assertNotNull("Test 1: Non-null value expected.",
                ns.processName("ak:hello", parts, false));
        assertEquals("Test 2: Incorrect namespace URI;", marketUri, parts[0]);
        assertEquals("Test 3: Incorrect local name;", "hello", parts[1]);
        assertEquals("Test 4: Incorrect raw name;", "ak:hello", parts[2]);

        assertNotNull("Test 5: Non-null value expected.",
                ns.processName("bk:", parts, false));
        assertEquals("Test 6: Incorrect namespace URI;", marketUri, parts[0]);
        assertEquals("Test 7: Incorrect local name;", "", parts[1]);
        assertEquals("Test 8: Incorrect raw name;", "bk:", parts[2]);

        assertNotNull("Test 9: Non-null value expected.",
                ns.processName("world", parts, false));
        assertEquals("Test 10: Incorrect namespace URI;", defaultUri, parts[0]);
        assertEquals("Test 11: Incorrect local name;", "world", parts[1]);
        assertEquals("Test 12: Incorrect raw name;", "world", parts[2]);

        assertNull("Test 13: Null expected for undeclared prefix.",
                ns.processName("ck:lorem", parts, false));

        assertNull("Test 14: Null expected for xmlns prefix.",
                ns.processName("xmlns:ipsum", parts, false));

        ns = new NamespaceSupport();
        ns.pushContext();
        assertNotNull("Test 15: Non-null value expected.",
                ns.processName("world", parts, false));
        assertEquals("Test 16: Incorrect namespace URI;", "", parts[0]);
        assertEquals("Test 17: Incorrect local name;", "world", parts[1]);
        assertEquals("Test 18: Incorrect raw name;", "world", parts[2]);
    }

    public void testProcessName_Attribute() {
        String[] parts = new String[3];

        assertNotNull("Test 1: Non-null value expected.",
                ns.processName("ak:hello", parts, true));
        assertEquals("Test 2: Incorrect namespace URI;", marketUri, parts[0]);
        assertEquals("Test 3: Incorrect local name;", "hello", parts[1]);
        assertEquals("Test 4: Incorrect raw name;", "ak:hello", parts[2]);

        assertNotNull("Test 5: Non-null value expected.",
                ns.processName("bk:", parts, true));
        assertEquals("Test 6: Incorrect namespace URI;", marketUri, parts[0]);
        assertEquals("Test 7: Incorrect local name;", "", parts[1]);
        assertEquals("Test 8: Incorrect raw name;", "bk:", parts[2]);

        assertNotNull("Test 9: Non-null value expected.",
                ns.processName("world", parts, true));
        assertEquals("Test 10: Incorrect namespace URI;", "", parts[0]);
        assertEquals("Test 11: Incorrect local name;", "world", parts[1]);
        assertEquals("Test 12: Incorrect raw name;", "world", parts[2]);

        assertNull("Test 13: Null expected for undeclared prefix.",
                ns.processName("ck:lorem", parts, true));

        assertNull("Test 14: Null expected for xmlns prefix.",
                ns.processName("xmlns:ipsum", parts, true));

        ns = new NamespaceSupport();
        ns.setNamespaceDeclUris(true);
        ns.pushContext();
        assertNotNull("Test 15: Non-null value expected.",
                ns.processName("xmlns", parts, true));
        assertEquals("Test 16: Incorrect namespace URI;", NamespaceSupport.NSDECL, parts[0]);
        assertEquals("Test 17: Incorrect local name;", "xmlns", parts[1]);
        assertEquals("Test 18: Incorrect raw name;", "xmlns", parts[2]);
    }

    @SuppressWarnings("unchecked")
    private int countPrefixes()
    {
        ArrayList<String> prefixes = Collections.list(ns.getPrefixes());
        return prefixes.size();
    }
}
