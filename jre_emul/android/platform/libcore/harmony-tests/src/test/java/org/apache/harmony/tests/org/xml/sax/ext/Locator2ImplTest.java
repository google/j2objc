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

import org.xml.sax.Locator;
import org.xml.sax.ext.Locator2Impl;
import org.xml.sax.helpers.LocatorImpl;

public class Locator2ImplTest extends TestCase {

    public static final String SYS = "mySystemID";

    public static final String PUB = "myPublicID";

    public static final int ROW = 1;

    public static final int COL = 2;

    public static final String ENC = "Klingon";

    public static final String XML = "1.0";

    public void testLocatorImpl() {
        Locator2Impl l = new Locator2Impl();

        assertEquals(null, l.getPublicId());
        assertEquals(null, l.getSystemId());
        assertEquals(0, l.getLineNumber());
        assertEquals(0, l.getColumnNumber());

        assertEquals(null, l.getEncoding());
        assertEquals(null, l.getXMLVersion());
    }

    public void testLocatorImplLocator() {
        Locator2Impl inner = new Locator2Impl();

        inner.setPublicId(PUB);
        inner.setSystemId(SYS);
        inner.setLineNumber(ROW);
        inner.setColumnNumber(COL);

        inner.setEncoding(ENC);
        inner.setXMLVersion(XML);

        // Ordinary case
        Locator2Impl outer = new Locator2Impl(inner);

        assertEquals(PUB, outer.getPublicId());
        assertEquals(SYS, outer.getSystemId());
        assertEquals(ROW, outer.getLineNumber());
        assertEquals(COL, outer.getColumnNumber());

        assertEquals(ENC, outer.getEncoding());
        assertEquals(XML, outer.getXMLVersion());

        // Instance of old locator
        outer = new Locator2Impl(new LocatorImpl(inner));

        assertEquals(PUB, outer.getPublicId());
        assertEquals(SYS, outer.getSystemId());
        assertEquals(ROW, outer.getLineNumber());
        assertEquals(COL, outer.getColumnNumber());

        assertEquals(null, outer.getEncoding());
        assertEquals(null, outer.getXMLVersion());

        // No locator
        try {
            outer = new Locator2Impl(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    public void testSetXMLVersionGetXMLVersion() {
        Locator2Impl l = new Locator2Impl();

        l.setXMLVersion(XML);
        assertEquals(XML, l.getXMLVersion());

        l.setXMLVersion(null);
        assertEquals(null, l.getXMLVersion());
    }

    public void testSetEncodingGetEncoding() {
        Locator2Impl l = new Locator2Impl();

        l.setEncoding(ENC);
        assertEquals(ENC, l.getEncoding());

        l.setEncoding(null);
        assertEquals(null, l.getEncoding());
    }

}
