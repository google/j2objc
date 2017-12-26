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

import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import java.io.IOException;

public class DefaultHandler2Test extends TestCase {

    private DefaultHandler2 h = new DefaultHandler2();

    public void testDefaultHandler2() {
        new DefaultHandler2();
    }

    public void testStartCDATA() {
        try {
            h.startCDATA();
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    public void testEndCDATA() {
        try {
            h.endCDATA();
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    public void testStartDTD() {
        try {
            h.startDTD("name", "publicId", "systemId");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    public void testEndDTD() {
        try {
            h.endDTD();
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    public void testStartEntity() {
        try {
            h.startEntity("name");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    public void testEndEntity() {
        try {
            h.endEntity("name");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    public void testComment() {
        try {
            h.comment("<!-- Comment -->".toCharArray(), 0, 15);
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    public void testAttributeDecl() {
        try {
            h.attributeDecl("eName", "aName", "type", "mode", "value");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    public void testElementDecl() {
        try {
            h.elementDecl("name", "model");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    public void testExternalEntityDecl() {
        try {
            h.externalEntityDecl("name", "publicId", "systemId");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    public void testInternalEntityDecl() {
        try {
            h.internalEntityDecl("name", "value");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    public void testGetExternalSubset() {
        try {
            assertNull(h.getExternalSubset("name", "http://some.uri"));
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    public void testResolveEntityStringString() {
        try {
            assertNull(h.resolveEntity("publicId", "systemId"));
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    public void testResolveEntityStringStringStringString() {
        try {
            assertNull(h.resolveEntity("name", "publicId", "http://some.uri",
                    "systemId"));
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

}
