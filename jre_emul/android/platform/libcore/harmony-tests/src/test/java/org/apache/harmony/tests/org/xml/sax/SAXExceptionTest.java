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

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class SAXExceptionTest extends TestCase {

    public static final String ERR = "Houston, we have a problem";

    public void testSAXParseException() {
        SAXException e = new SAXException();

        assertNull(e.getMessage());
        assertNull(e.getException());
    }

    public void testSAXException_String_Exception() {
        Exception c = new Exception();

        // Ordinary case
        SAXException e = new SAXException(ERR, c);

        assertEquals(ERR, e.getMessage());
        assertEquals(c, e.getException());

        // No message
        e = new SAXException(null, c);

        assertNull(e.getMessage());
        assertEquals(c, e.getException());

        // No cause
        e = new SAXParseException(ERR, null);

        assertEquals(ERR, e.getMessage());
        assertNull(e.getException());
    }

    public void testSAXException_String() {
        // Ordinary case
        SAXException e = new SAXException(ERR);

        assertEquals(ERR, e.getMessage());
        assertNull(e.getException());

        // No message
        e = new SAXException((String)null);

        assertNull(e.getMessage());
        assertNull(e.getException());
    }

    public void testSAXException_Exception() {
        Exception c = new Exception();

        // Ordinary case
        SAXException e = new SAXException(c);

        assertNull(e.getMessage());
        assertEquals(c, e.getException());

        // No cause
        e = new SAXException((Exception)null);

        assertNull(e.getMessage());
        assertNull(e.getException());
    }

    public void testToString() {
        // Ordinary case
        SAXException e = new SAXException(ERR);
        String s = e.toString();

        assertTrue(s.contains(ERR));

        // No message
        e = new SAXException();
        s = e.toString();

        assertFalse(s.contains(ERR));
   }

}
