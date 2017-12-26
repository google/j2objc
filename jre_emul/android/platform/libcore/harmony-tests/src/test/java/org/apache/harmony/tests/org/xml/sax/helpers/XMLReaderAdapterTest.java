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
import java.util.Locale;

import junit.framework.TestCase;

import org.apache.harmony.tests.org.xml.sax.support.MethodLogger;
import org.apache.harmony.tests.org.xml.sax.support.MockHandler;
import org.apache.harmony.tests.org.xml.sax.support.MockReader;
import org.apache.harmony.tests.org.xml.sax.support.MockResolver;
import org.xml.sax.AttributeList;
import org.xml.sax.Attributes;
import org.xml.sax.DTDHandler;
import org.xml.sax.DocumentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.LocatorImpl;
import org.xml.sax.helpers.XMLReaderAdapter;

@SuppressWarnings("deprecation")
public class XMLReaderAdapterTest extends TestCase {

    // Note: In many cases we can only test that delegation works
    // properly. The rest is outside the scope of the specification.

    private MethodLogger logger = new MethodLogger();

    private MockHandler handler = new MockHandler(logger);

    private XMLReader reader = new MockReader(logger);

    private XMLReaderAdapter adapter = new XMLReaderAdapter(reader);

    private void assertEquals(Object[] a, Object[] b) {
        assertEquals(a.length, b.length);

        for (int i = 0; i < a.length; i++) {
            assertEquals("Element #" + i + " must be equal", a[i], b[i]);
        }
    }

    @Override
    public void setUp() {
        adapter.setDocumentHandler(handler);
        adapter.setDTDHandler(handler);
        adapter.setErrorHandler(handler);
    }

    @Override protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testXMLReaderAdapter() {
        System.setProperty("org.xml.sax.driver",
                "org.apache.harmony.tests.org.xml.sax.support.DoNothingXMLReader");

        try {
            new XMLReaderAdapter();
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    public void testXMLReaderAdapterXMLReader() {
        // Ordinary case
        @SuppressWarnings("unused")
        XMLReaderAdapter adapter = new XMLReaderAdapter(reader);

        // Null case
        try {
            adapter = new XMLReaderAdapter(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    public void testSetLocale() {
        // SAX RI does not support this, hence always expect exception
        try {
            adapter.setLocale(Locale.getDefault());
            fail("SAXException expected");
        } catch (SAXException e) {
            // Expected
        }
    }

    public void testSetEntityResolver() {
        EntityResolver resolver = new MockResolver();

        // Ordinary case
        adapter.setEntityResolver(resolver);
        assertEquals(resolver, reader.getEntityResolver());

        // null case
        adapter.setEntityResolver(null);
        assertEquals(null, reader.getEntityResolver());
    }

    public void testSetDTDHandler() {
        // Ordinary case
        assertEquals(handler, reader.getDTDHandler());

        // null case
        adapter.setDTDHandler(null);
        assertEquals(null, reader.getDTDHandler());
    }

    public void testSetDocumentHandler() {
        // There is no getter for the DocumentHandler, so we can only test
        // indirectly whether is has been set correctly.
        try {
            adapter.startDocument();
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        assertEquals("startDocument", logger.getMethod());
        assertEquals(new Object[] { }, logger.getArgs());

        // null case
        adapter.setDocumentHandler(null);
    }

    public void testSetErrorHandler() {
        // Ordinary case
        assertEquals(handler, reader.getErrorHandler());

        // null case
        adapter.setErrorHandler(null);
        assertEquals(null, reader.getErrorHandler());
    }

    public void testParseString() {
        try {
            adapter.parse("foo");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        // The SAX RI creates an InputSource itself and then delegates to the
        // "other" parse method.
        assertEquals("parse", logger.getMethod(0));
        assertEquals(InputSource.class, logger.getArgs(0)[0].getClass());
    }

    public void testParseInputSource() {
        InputSource source = new InputSource("foo");

        try {
            adapter.parse(source);
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        assertEquals("parse", logger.getMethod());
        assertEquals(new Object[] { source }, logger.getArgs());
    }

    public void testSetDocumentLocator() {
        // Ordinary case
        LocatorImpl locator = new LocatorImpl();
        adapter.setDocumentLocator(locator);

        assertEquals("setDocumentLocator", logger.getMethod());
        assertEquals(new Object[] { locator }, logger.getArgs());

        // null case (for the DocumentHandler itself!)
        adapter.setDocumentHandler(null);
        adapter.setDocumentLocator(locator);
    }

    public void testStartDocument() {
        try {
            adapter.startDocument();
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        assertEquals(logger.size(), 1);
        assertEquals("startDocument", logger.getMethod());
        assertEquals(new Object[] {}, logger.getArgs());
    }

    public void testEndDocument() {
        try {
            adapter.endDocument();
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        assertEquals(logger.size(), 1);
        assertEquals("endDocument", logger.getMethod());
        assertEquals(new Object[] {}, logger.getArgs());
    }

    public void testStartPrefixMapping() {
        adapter.startPrefixMapping("foo", "http://some.uri");
        assertEquals(logger.size(), 0);
    }

    public void testEndPrefixMapping() {
        adapter.endPrefixMapping("foo");
        assertEquals(logger.size(), 0);
    }

    public void testStartElement() {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("http://some.other.uri", "gabba", "gabba:hey",
                "int", "42");

        try {
            adapter.startElement("http://some.uri", "bar", "foo:bar", atts);
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        assertEquals(logger.size(), 1);
        assertEquals("startElement", logger.getMethod());
        assertEquals("foo:bar", logger.getArgs()[0]);
        assertEquals("gabba:hey",
                ((AttributeList)logger.getArgs()[1]).getName(0));
    }

    public void testEndElement() {
        try {
            adapter.endElement("http://some.uri", "bar", "foo:bar");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        assertEquals(logger.size(), 1);
        assertEquals("endElement", logger.getMethod());
        assertEquals(new Object[] { "foo:bar" }, logger.getArgs());
    }

    public void testCharacters() {
        char[] ch = "Android".toCharArray();

        try {
            adapter.characters(ch, 2, 5);
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
            adapter.ignorableWhitespace(ch, 0, 5);
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        assertEquals(logger.size(), 1);
        assertEquals("ignorableWhitespace", logger.getMethod());
        assertEquals(new Object[] { ch, 0, 5 }, logger.getArgs());
    }

    public void testProcessingInstruction() {
        try {
            adapter.processingInstruction("foo", "bar");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        assertEquals(logger.size(), 1);
        assertEquals("processingInstruction", logger.getMethod());
        assertEquals(new Object[] { "foo" , "bar" }, logger.getArgs());
    }

    public void testSkippedEntity() {
        try {
            adapter.skippedEntity("foo");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        assertEquals(logger.size(), 0);
    }

}
