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

package org.apache.harmony.tests.org.xml.sax;

import junit.framework.TestCase;

import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.LocatorImpl;

public class SAXParseExceptionTest extends TestCase {

    public static final String ERR = "Houston, we have a problem";

    public static final String SYS = "mySystemID";

    public static final String PUB = "myPublicID";

    public static final int ROW = 1;

    public static final int COL = 2;

    public void testSAXParseException_String_Locator_Exception() {
        LocatorImpl l = new LocatorImpl();
        l.setPublicId(PUB);
        l.setSystemId(SYS);
        l.setLineNumber(ROW);
        l.setColumnNumber(COL);

        Exception c = new Exception();

        // Ordinary case
        SAXParseException e = new SAXParseException(ERR, l, c);

        assertEquals(ERR, e.getMessage());
        assertEquals(c, e.getException());

        assertEquals(PUB, e.getPublicId());
        assertEquals(SYS, e.getSystemId());
        assertEquals(ROW, e.getLineNumber());
        assertEquals(COL, e.getColumnNumber());

        // No message
        e = new SAXParseException(null, l, c);

        assertNull(e.getMessage());
        assertEquals(c, e.getException());

        assertEquals(PUB, e.getPublicId());
        assertEquals(SYS, e.getSystemId());
        assertEquals(ROW, e.getLineNumber());
        assertEquals(COL, e.getColumnNumber());

        // No locator
        e = new SAXParseException(ERR, null, c);

        assertEquals(ERR, e.getMessage());
        assertEquals(c, e.getException());

        assertNull(e.getPublicId());
        assertNull(e.getSystemId());
        assertEquals(-1, e.getLineNumber());
        assertEquals(-1, e.getColumnNumber());

        // No cause
        e = new SAXParseException(ERR, l, null);

        assertEquals(ERR, e.getMessage());
        assertNull(e.getException());

        assertEquals(PUB, e.getPublicId());
        assertEquals(SYS, e.getSystemId());
        assertEquals(ROW, e.getLineNumber());
        assertEquals(COL, e.getColumnNumber());
    }

    public void testSAXParseException_String_Locator() {
        LocatorImpl l = new LocatorImpl();
        l.setPublicId(PUB);
        l.setSystemId(SYS);
        l.setLineNumber(ROW);
        l.setColumnNumber(COL);

        // Ordinary case
        SAXParseException e = new SAXParseException(ERR, l);

        assertEquals(ERR, e.getMessage());
        assertNull(e.getException());

        assertEquals(PUB, e.getPublicId());
        assertEquals(SYS, e.getSystemId());
        assertEquals(ROW, e.getLineNumber());
        assertEquals(COL, e.getColumnNumber());

        // No message
        e = new SAXParseException(null, l);

        assertNull(e.getMessage());
        assertNull(e.getException());

        assertEquals(PUB, e.getPublicId());
        assertEquals(SYS, e.getSystemId());
        assertEquals(ROW, e.getLineNumber());
        assertEquals(COL, e.getColumnNumber());

        // No locator
        e = new SAXParseException(ERR, null);

        assertEquals(ERR, e.getMessage());
        assertNull(e.getException());

        assertNull(e.getPublicId());
        assertNull(e.getSystemId());
        assertEquals(-1, e.getLineNumber());
        assertEquals(-1, e.getColumnNumber());

    }

    public void testSAXParseException_String_String_String_int_int_Exception() {
        Exception c = new Exception();

        // Ordinary case
        SAXParseException e = new SAXParseException(ERR, PUB, SYS, ROW, COL, c);

        assertEquals(ERR, e.getMessage());
        assertEquals(c, e.getException());

        assertEquals(PUB, e.getPublicId());
        assertEquals(SYS, e.getSystemId());
        assertEquals(ROW, e.getLineNumber());
        assertEquals(COL, e.getColumnNumber());

        // No message
        e = new SAXParseException(null, PUB, SYS, ROW, COL, c);

        assertNull(e.getMessage());
        assertEquals(c, e.getException());

        assertEquals(PUB, e.getPublicId());
        assertEquals(SYS, e.getSystemId());
        assertEquals(ROW, e.getLineNumber());
        assertEquals(COL, e.getColumnNumber());

        // No locator
        e = new SAXParseException(ERR, null, null, -1, -1, c);

        assertEquals(ERR, e.getMessage());
        assertEquals(c, e.getException());

        assertNull(e.getPublicId());
        assertNull(e.getSystemId());
        assertEquals(-1, e.getLineNumber());
        assertEquals(-1, e.getColumnNumber());

        // No cause
        e = new SAXParseException(ERR, PUB, SYS, ROW, COL, null);

        assertEquals(ERR, e.getMessage());
        assertNull(e.getException());

        assertEquals(PUB, e.getPublicId());
        assertEquals(SYS, e.getSystemId());
        assertEquals(ROW, e.getLineNumber());
        assertEquals(COL, e.getColumnNumber());
    }

    public void testSAXParseException_String_String_String_int_int() {
        // Ordinary case
        SAXParseException e = new SAXParseException(ERR, PUB, SYS, ROW, COL);

        assertEquals(ERR, e.getMessage());
        assertNull(e.getException());

        assertEquals(PUB, e.getPublicId());
        assertEquals(SYS, e.getSystemId());
        assertEquals(ROW, e.getLineNumber());
        assertEquals(COL, e.getColumnNumber());

        // No message
        e = new SAXParseException(null, PUB, SYS, ROW, COL);

        assertNull(e.getMessage());
        assertNull(e.getException());

        assertEquals(PUB, e.getPublicId());
        assertEquals(SYS, e.getSystemId());
        assertEquals(ROW, e.getLineNumber());
        assertEquals(COL, e.getColumnNumber());

        // No locator
        e = new SAXParseException(ERR, null, null, -1, -1);

        assertEquals(ERR, e.getMessage());
        assertNull(e.getException());

        assertNull(e.getPublicId());
        assertNull(e.getSystemId());
        assertEquals(-1, e.getLineNumber());
        assertEquals(-1, e.getColumnNumber());
    }

}
