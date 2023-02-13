/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package libcore.javax.xml.namespace;

import javax.xml.namespace.QName;
import junit.framework.TestCase;

public class QNameTest extends TestCase {

    public void testConstructor() {
        QName qName = new QName("hello");
        assertEquals("", qName.getNamespaceURI());
        assertEquals("", qName.getPrefix());
        assertEquals("hello", qName.getLocalPart());
    }

    public void testEquals() {
        QName qName = new QName("namespace", "local part", "prefix");
        assertTrue(qName.equals(qName));
        assertFalse(qName.equals(null));
        assertFalse(qName.equals(new Object()));

        QName qNameSame = new QName("namespace", "local part", "prefix");
        assertTrue(qName.equals(qNameSame));
        assertTrue(qNameSame.equals(qName));

        // Check the namespace is considered in equality considerations.
        QName qNameDifferentNamespace = new QName("another namespace", "local part", "prefix");
        assertFalse(qName.equals(qNameDifferentNamespace));
        assertFalse(qNameDifferentNamespace.equals(qName));

        // Check the local part is considered in equality considerations.
        QName qNameDifferentLocalPart = new QName("namespace", "another local part", "prefix");
        assertFalse(qName.equals(qNameDifferentLocalPart));
        assertFalse(qNameDifferentLocalPart.equals(qName));

        // Check the prefix is not considered in equality considerations.
        QName qNameDifferentPrefix = new QName("namespace", "local part", "another prefix");
        assertTrue(qName.equals(qNameDifferentPrefix));
        assertTrue(qNameDifferentPrefix.equals(qName));
    }

    public void testGetNamespaceURI() {
        QName qName = new QName("namespace", "local part", "prefix");
        assertEquals("namespace", qName.getNamespaceURI());
        assertEquals("local part", qName.getLocalPart());
        assertEquals("prefix", qName.getPrefix());

        qName = new QName(null, "local part", "prefix");
        assertEquals("", qName.getNamespaceURI());
        assertEquals("local part", qName.getLocalPart());
        assertEquals("prefix", qName.getPrefix());
    }

    public void testGetPrefix() {
        QName qName = new QName("namespace", "local part", "prefix");
        assertEquals("prefix", qName.getPrefix());

        try {
            new QName("namespace", "local part", null);
            fail("Unexpectedly didn't throw IllegalArgumentException");
        } catch (IllegalArgumentException expected) {}
    }
}
