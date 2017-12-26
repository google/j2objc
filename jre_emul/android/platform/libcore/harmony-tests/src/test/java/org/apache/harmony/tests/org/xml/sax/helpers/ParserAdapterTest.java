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
import org.apache.harmony.tests.org.xml.sax.support.MockHandler;
import org.apache.harmony.tests.org.xml.sax.support.MockParser;
import org.apache.harmony.tests.org.xml.sax.support.MockResolver;
import org.xml.sax.AttributeList;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.AttributeListImpl;
import org.xml.sax.helpers.LocatorImpl;
import org.xml.sax.helpers.ParserAdapter;

@SuppressWarnings("deprecation")
public class ParserAdapterTest extends TestCase {

    // Note: In many cases we can only test that delegation works
    // properly. The rest is outside the scope of the specification.

    private final static String FEATURES = "http://xml.org/sax/features/";

    private final static String NAMESPACES = FEATURES + "namespaces";

    private final static String NAMESPACE_PREFIXES = FEATURES
                                                        + "namespace-prefixes";

    private final static String XMLNS_URIs = FEATURES + "xmlns-uris";

    private MethodLogger logger = new MethodLogger();

    private MockHandler handler = new MockHandler(logger);

    private Parser parser = new MockParser(logger);

    private ParserAdapter adapter = new ParserAdapter(parser);

    private void assertEquals(Object[] a, Object[] b) {
        assertEquals(a.length, b.length);

        for (int i = 0; i < a.length; i++) {
            assertEquals("Element #" + i + " must be equal", a[i], b[i]);
        }
    }

    @Override
    public void setUp() {
        adapter.setContentHandler(handler);
        adapter.setDTDHandler(handler);
        adapter.setErrorHandler(handler);
    }

    @Override protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testParserAdapter() {
        System.setProperty("org.xml.sax.parser",
                "org.apache.harmony.tests.org.xml.sax.support.DoNothingParser");

        try {
            new ParserAdapter();
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    public void testParserAdapterParser() {
        // Ordinary case
        @SuppressWarnings("unused")
        ParserAdapter adapter = new ParserAdapter(parser);

        // Null case
        try {
            adapter = new ParserAdapter(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    public void testGetSetFeature() {
        String[] features = new String[] { NAMESPACES, NAMESPACE_PREFIXES,
                XMLNS_URIs };

        for (String s: features) {
            try {
                adapter.setFeature(s, true);
                assertEquals(true, adapter.getFeature(s));

                adapter.setFeature(s, false);
                assertEquals(false, adapter.getFeature(s));
            } catch (SAXException e) {
                throw new RuntimeException("Unexpected exception", e);
            }
        }

        try {
            adapter.setFeature("http://argle.bargle", true);
            fail("SAXNotRecognizedException expected");
        } catch (SAXNotRecognizedException e) {
            // Expected
        } catch (SAXNotSupportedException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    public void testGetSetProperty() {
        try {
            adapter.setProperty("http://argle.bargle", ":)");
            fail("SAXNotRecognizedException expected");
        } catch (SAXNotRecognizedException e) {
            // Expected
        } catch (SAXNotSupportedException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        try {
            adapter.getProperty("http://argle.bargle");
            fail("SAXNotRecognizedException expected");
        } catch (SAXNotRecognizedException e) {
            // Expected
        } catch (SAXNotSupportedException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    public void testGetSetEntityResolver() {
        EntityResolver resolver = new MockResolver();

        adapter.setEntityResolver(resolver);
        assertEquals(resolver, adapter.getEntityResolver());

        adapter.setEntityResolver(null);
        assertEquals(null, adapter.getEntityResolver());
    }

    public void testGetSetDTDHandler() {
        adapter.setDTDHandler(null);
        assertEquals(null, adapter.getDTDHandler());

        adapter.setDTDHandler(handler);
        assertEquals(handler, adapter.getDTDHandler());
    }

    public void testGetSetContentHandler() {
        adapter.setContentHandler(null);
        assertEquals(null, adapter.getContentHandler());

        adapter.setContentHandler(handler);
        assertEquals(handler, adapter.getContentHandler());
    }

    public void testGetSetErrorHandler() {
        adapter.setErrorHandler(null);
        assertEquals(null, adapter.getErrorHandler());

        adapter.setErrorHandler(handler);
        assertEquals(handler, adapter.getErrorHandler());
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
        assertEquals("parse", logger.getMethod());
        assertEquals(InputSource.class, logger.getArgs()[0].getClass());
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
        Locator l = new LocatorImpl();

        adapter.setDocumentLocator(l);

        assertEquals(logger.size(), 1);
        assertEquals("setDocumentLocator", logger.getMethod());
        assertEquals(new Object[] { l }, logger.getArgs());

        adapter.setDocumentLocator(null);

        assertEquals(logger.size(), 2);
        assertEquals("setDocumentLocator", logger.getMethod());
        assertEquals(new Object[] { null }, logger.getArgs());
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

    public void testStartElement() {
        AttributeListImpl atts = new AttributeListImpl();
        atts.addAttribute("john:doe", "int", "42");

        try {
            adapter.startDocument();
            adapter.startElement("foo:bar", atts);
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        assertEquals("startElement", logger.getMethod());
        assertEquals("", logger.getArgs()[0]);
        assertEquals("", logger.getArgs()[1]);
        assertEquals("foo:bar", logger.getArgs()[2]);
        assertEquals("john:doe", ((Attributes)logger.getArgs()[3]).getQName(0));
    }

    public void testEndElement() {
        AttributeListImpl atts = new AttributeListImpl();
        atts.addAttribute("john:doe", "int", "42");

        try {
            adapter.startDocument();
            adapter.startElement("foo:bar", atts);
            adapter.endElement("foo:bar");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        assertEquals("endElement", logger.getMethod());
        assertEquals(new String[] { "", "", "foo:bar" }, logger.getArgs());
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

}
