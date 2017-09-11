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

import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

public class LocatorImplTest extends TestCase {

    public static final String SYS = "mySystemID";

    public static final String PUB = "myPublicID";

    public static final int ROW = 1;

    public static final int COL = 2;

    public void testLocatorImpl() {
        LocatorImpl l = new LocatorImpl();

        assertEquals(null, l.getPublicId());
        assertEquals(null, l.getSystemId());
        assertEquals(0, l.getLineNumber());
        assertEquals(0, l.getColumnNumber());
    }

    public void testLocatorImplLocator() {
        LocatorImpl inner = new LocatorImpl();

        inner.setPublicId(PUB);
        inner.setSystemId(SYS);
        inner.setLineNumber(ROW);
        inner.setColumnNumber(COL);

        // Ordinary case
        LocatorImpl outer = new LocatorImpl(inner);

        assertEquals(PUB, outer.getPublicId());
        assertEquals(SYS, outer.getSystemId());
        assertEquals(ROW, outer.getLineNumber());
        assertEquals(COL, outer.getColumnNumber());

        // No locator
        try {
            outer = new LocatorImpl(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    public void testSetPublicIdGetPublicId() {
        LocatorImpl l = new LocatorImpl();

        l.setPublicId(PUB);
        assertEquals(PUB, l.getPublicId());

        l.setPublicId(null);
        assertEquals(null, l.getPublicId());
    }

    public void testSetSystemIdGetSystemId() {
        LocatorImpl l = new LocatorImpl();

        l.setSystemId(SYS);
        assertEquals(SYS, l.getSystemId());

        l.setSystemId(null);
        assertEquals(null, l.getSystemId());
    }

    public void testSetLineNumberGetLineNumber() {
        LocatorImpl l = new LocatorImpl();

        l.setLineNumber(ROW);
        assertEquals(ROW, l.getLineNumber());

        l.setLineNumber(0);
        assertEquals(0, l.getLineNumber());
    }

    public void testSetColumnNumberGetColumnNumber() {
        LocatorImpl l = new LocatorImpl();

        l.setColumnNumber(COL);
        assertEquals(COL, l.getColumnNumber());

        l.setColumnNumber(0);
        assertEquals(0, l.getColumnNumber());
    }

}
