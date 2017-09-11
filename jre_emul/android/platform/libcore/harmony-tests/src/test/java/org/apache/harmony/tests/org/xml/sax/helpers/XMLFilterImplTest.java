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

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.harmony.tests.org.xml.sax.support.MethodLogger;
import org.apache.harmony.tests.org.xml.sax.support.MockFilter;
import org.apache.harmony.tests.org.xml.sax.support.MockHandler;
import org.apache.harmony.tests.org.xml.sax.support.MockResolver;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.LocatorImpl;
import org.xml.sax.helpers.XMLFilterImpl;


public class XMLFilterImplTest extends TestCase {

    // Note: In many cases we can only test that delegation works
    // properly. The rest is outside the scope of the specification.

    private MethodLogger logger = new MethodLogger();

    private MockHandler handler = new MockHandler(logger);

    private XMLFilterImpl parent = new MockFilter(logger);

    private XMLFilterImpl child = new XMLFilterImpl(parent);

    private XMLFilterImpl orphan = new XMLFilterImpl();

    private void assertEquals(Object[] a, Object[] b) {
        assertEquals(a.length, b.length);

        for (int i = 0; i < a.length; i++) {
            assertEquals("Element #" + i + " must be equal", a[i], b[i]);
        }
    }

    public void setUp() {
        parent.setContentHandler(handler);
        parent.setDTDHandler(handler);
        parent.setErrorHandler(handler);

        child.setContentHandler(handler);
        child.setDTDHandler(handler);
        child.setErrorHandler(handler);
    }

    public void testXMLFilterImpl() {
        assertEquals(null, parent.getParent());
    }

    public void testXMLFilterImplXMLReader() {
        // Ordinary case
        assertEquals(null, parent.getParent());

        // null case
        XMLFilterImpl filter = new XMLFilterImpl(null);
        assertEquals(null, filter.getParent());
    }

    public void testGetSetParent() {
        child.setParent(null);
        assertEquals(null, child.getParent());

        child.setParent(parent);
        assertEquals(parent, child.getParent());
    }

    public void testGetSetFeature() {
        // Ordinary case
        try {
            child.setFeature("foo", true);
            assertEquals(true, child.getFeature("foo"));

            child.setFeature("foo", false);
            assertEquals(false, child.getFeature("foo"));
        } catch (SAXNotRecognizedException e) {
            throw new RuntimeException("Unexpected exception", e);
        } catch (SAXNotSupportedException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        // No parent case
        try {
            orphan.setFeature("foo", false);
            fail("SAXNotRecognizedException expected");
        } catch (SAXNotRecognizedException e) {
            // Expected
        } catch (SAXNotSupportedException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    public void testGetSetProperty() {
        // Ordinary case
        try {
            child.setProperty("foo", "bar");
            assertEquals("bar", child.getProperty("foo"));

            child.setProperty("foo", null);
            assertEquals(null, child.getProperty("foo"));
        } catch (SAXNotRecognizedException e) {
            throw new RuntimeException("Unexpected exception", e);
        } catch (SAXNotSupportedException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        // No parent case
        try {
            orphan.setProperty("foo", "bar");
            fail("SAXNotRecognizedException expected");
        } catch (SAXNotRecognizedException e) {
            // Expected
        } catch (SAXNotSupportedException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    public void testGetSetEntityResolver() {
        EntityResolver resolver = new MockResolver();

        parent.setEntityResolver(resolver);
        assertEquals(resolver, parent.getEntityResolver());

        parent.setEntityResolver(null);
        assertEquals(null, parent.getEntityResolver());
    }

    public void testGetSetDTDHandler() {
        parent.setDTDHandler(null);
        assertEquals(null, parent.getDTDHandler());

        parent.setDTDHandler(handler);
        assertEquals(handler, parent.getDTDHandler());
    }

    public void testGetSetContentHandler() {
        parent.setContentHandler(null);
        assertEquals(null, parent.getContentHandler());

        parent.setContentHandler(handler);
        assertEquals(handler, parent.getContentHandler());
    }

    public void testGetSetErrorHandler() {
        parent.setErrorHandler(null);
        assertEquals(null, parent.getErrorHandler());

        parent.setErrorHandler(handler);
        assertEquals(handler, parent.getErrorHandler());
    }

    public void testParseInputSource() {
        InputSource is = new InputSource();

        // Ordinary case
        try {
            child.parse(is);
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        assertEquals(1, logger.size());
        assertEquals("parse", logger.getMethod());

        // No parent case
        try {
            orphan.parse(is);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    public void testParseString() {
        // Ordinary case
        try {
            child.parse("foo");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        assertEquals(1, logger.size());
        assertEquals("parse", logger.getMethod());

        // No parent case
        try {
            orphan.parse("foo");
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    public void testResolveEntity() {
        InputSource expected = new InputSource();

        MockResolver resolver = new MockResolver();
        resolver.addEntity("foo", "bar", expected);

        InputSource result = null;

        parent.setEntityResolver(resolver);

        // Ordinary case
        try {
            result = parent.resolveEntity("foo", "bar");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        assertEquals(expected, result);

        // No entity resolver case
        parent.setEntityResolver(null);

        try {
            result = parent.resolveEntity("foo", "bar");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        assertEquals(null, result);
    }

    public void testNotationDecl() {
        try {
            parent.notationDecl("foo", "bar", "foobar");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        assertEquals(logger.size(), 1);
        assertEquals("notationDecl", logger.getMethod());
        assertEquals(new Object[] { "foo", "bar", "foobar" },
                logger.getArgs());
    }

    public void testUnparsedEntityDecl() {
        try {
            parent.unparsedEntityDecl("foo", "bar", "gabba", "hey");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        assertEquals(logger.size(), 1);
        assertEquals("unparsedEntityDecl", logger.getMethod());
        assertEquals(new Object[] { "foo", "bar", "gabba", "hey" },
                logger.getArgs());
    }

    public void testSetDocumentLocator() {
        Locator l = new LocatorImpl();

        child.setDocumentLocator(l);

        assertEquals(logger.size(), 1);
        assertEquals("setDocumentLocator", logger.getMethod());
        assertEquals(new Object[] { l }, logger.getArgs());

        child.setDocumentLocator(null);

        assertEquals(logger.size(), 2);
        assertEquals("setDocumentLocator", logger.getMethod());
        assertEquals(new Object[] { null }, logger.getArgs());
    }

    public void testStartDocument() {
        try {
            parent.startDocument();
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        assertEquals(logger.size(), 1);
        assertEquals("startDocument", logger.getMethod());
        assertEquals(new Object[] {}, logger.getArgs());
    }

    public void testEndDocument() {
        try {
            parent.endDocument();
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        assertEquals(logger.size(), 1);
        assertEquals("endDocument", logger.getMethod());
        assertEquals(new Object[] {}, logger.getArgs());
    }

    public void testStartPrefixMapping() {
        try {
            parent.startPrefixMapping("foo", "http://some.uri");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        assertEquals(logger.size(), 1);
        assertEquals("startPrefixMapping", logger.getMethod());
        assertEquals(new Object[] { "foo", "http://some.uri" },
                logger.getArgs());
    }

    public void testEndPrefixMapping() {
        try {
            parent.endPrefixMapping("foo");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        assertEquals(logger.size(), 1);
        assertEquals("endPrefixMapping", logger.getMethod());
        assertEquals(new Object[] { "foo" }, logger.getArgs());
    }

    public void testStartElement() {
        Attributes atts = new AttributesImpl();

        try {
            parent.startElement("http://some.uri", "bar", "foo:bar", atts);
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        assertEquals(logger.size(), 1);
        assertEquals("startElement", logger.getMethod());
        assertEquals(new Object[] { "http://some.uri", "bar", "foo:bar", atts },
                logger.getArgs());
    }

    public void testEndElement() {
        try {
            parent.endElement("http://some.uri", "bar", "foo:bar");
         } catch (SAXException e) {
             throw new RuntimeException("Unexpected exception", e);
         }

         assertEquals(logger.size(), 1);
         assertEquals("endElement", logger.getMethod());
         assertEquals(new Object[] { "http://some.uri", "bar", "foo:bar" },
                 logger.getArgs());
    }

    public void testCharacters() {
        char[] ch = "Android".toCharArray();

        try {
            parent.characters(ch, 2, 5);
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        assertEquals(logger.size(), 1);
        assertEquals("characters", logger.getMethod());
        assertEquals(new Object[] { ch, 2, 5 }, logger.getArgs());
    }

    public void testIgnorableWhitespace() {
        char[] ch = "     ".toCharArray();

        try {
            parent.ignorableWhitespace(ch, 0, 5);
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        assertEquals(logger.size(), 1);
        assertEquals("ignorableWhitespace", logger.getMethod());
        assertEquals(new Object[] { ch, 0, 5 }, logger.getArgs());
    }

    public void testProcessingInstruction() {
        try {
            parent.processingInstruction("foo", "bar");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        assertEquals(logger.size(), 1);
        assertEquals("processingInstruction", logger.getMethod());
        assertEquals(new Object[] { "foo", "bar" }, logger.getArgs());
    }

    public void testSkippedEntity() {
        try {
            parent.skippedEntity("foo");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        assertEquals(logger.size(), 1);
        assertEquals("skippedEntity", logger.getMethod());
        assertEquals(new Object[] { "foo" }, logger.getArgs());
    }

    public void testWarning() {
        SAXParseException exception = new SAXParseException("Oops!", null);

        try {
            parent.warning(exception);
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        assertEquals(logger.size(), 1);
        assertEquals("warning", logger.getMethod());
        assertEquals(new Object[] { exception }, logger.getArgs());
    }

    public void testError() {
        SAXParseException exception = new SAXParseException("Oops!", null);

        try {
            parent.error(exception);
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        assertEquals(logger.size(), 1);
        assertEquals("error", logger.getMethod());
        assertEquals(new Object[] { exception }, logger.getArgs());
    }

    public void testFatalError() {
        SAXParseException exception = new SAXParseException("Oops!", null);

        try {
            parent.fatalError(exception);
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        assertEquals(logger.size(), 1);
        assertEquals("fatalError", logger.getMethod());
        assertEquals(new Object[] { exception }, logger.getArgs());
    }

}
