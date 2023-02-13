/*
 * Copyright (C) 2007 The Android Open Source Project
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

package org.apache.harmony.tests.javax.xml.parsers;

import junit.framework.TestCase;

import org.apache.harmony.tests.org.xml.sax.support.MethodLogger;
import org.apache.harmony.tests.org.xml.sax.support.MockHandler;
import org.apache.harmony.tests.org.xml.sax.support.MockResolver;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import tests.support.resource.Support_Resources;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class DocumentBuilderTest extends TestCase {

    private class MockDocumentBuilder extends DocumentBuilder {

        public MockDocumentBuilder() {
            super();
        }

        /*
         * @see javax.xml.parsers.DocumentBuilder#getDOMImplementation()
         */
        @Override
        public DOMImplementation getDOMImplementation() {
            // it is a fake
            return null;
        }

        /*
         * @see javax.xml.parsers.DocumentBuilder#isNamespaceAware()
         */
        @Override
        public boolean isNamespaceAware() {
            // it is a fake
            return false;
        }

        /*
         * @see javax.xml.parsers.DocumentBuilder#isValidating()
         */
        @Override
        public boolean isValidating() {
            // it is a fake
            return false;
        }

        /*
         * @see javax.xml.parsers.DocumentBuilder#newDocument()
         */
        @Override
        public Document newDocument() {
            // it is a fake
            return null;
        }

        /*
         * @see javax.xml.parsers.DocumentBuilder#parse(org.xml.sax.InputSource)
         */
        @Override
        public Document parse(InputSource is) throws SAXException, IOException {
            // it is a fake
            return null;
        }

        /*
         * @see javax.xml.parsers.DocumentBuilder#setEntityResolver(
         *  org.xml.sax.EntityResolver)
         */
        @Override
        public void setEntityResolver(EntityResolver er) {
            // it is a fake
        }

        /*
         * @see javax.xml.parsers.DocumentBuilder#setErrorHandler(
         *  org.xml.sax.ErrorHandler)
         */
        @Override
        public void setErrorHandler(ErrorHandler eh) {
            // it is a fake
        }

        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

    DocumentBuilderFactory dbf;

    DocumentBuilder db;

    protected void setUp() throws Exception {

        dbf = DocumentBuilderFactory.newInstance();

        dbf.setIgnoringElementContentWhitespace(true);

        db = dbf.newDocumentBuilder();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testDocumentBuilder() {
        try {
            new MockDocumentBuilder();
        } catch (Exception e) {
            fail("unexpected exception " + e.toString());
        }
    }

    /**
     *  javax.xml.parsers.DocumentBuilder#getSchema()
     *  TBD getSchema() is not supported
     */
 /*   public void test_getSchema() {
        assertNull(db.getSchema());
        SchemaFactory sf =
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            Schema schema = sf.newSchema();
            dbf.setSchema(schema);
            assertNotNull(dbf.newDocumentBuilder().getSchema());
        } catch (ParserConfigurationException pce) {
            fail("Unexpected ParserConfigurationException " + pce.toString());
        } catch (SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }
    }
*/
    public void testNewDocument() {
        Document d;

        try {
            d = dbf.newDocumentBuilder().newDocument();
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        assertNotNull(d);
        assertNull(d.getDoctype());
        assertNull(d.getDocumentElement());
        assertNull(d.getNamespaceURI());
    }

    public void testGetImplementation() {
        DOMImplementation d;

        try {
            d = dbf.newDocumentBuilder().getDOMImplementation();
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        assertNotNull(d);
    }

    public void testIsNamespaceAware() {
        try {
            dbf.setNamespaceAware(true);
            assertTrue(dbf.newDocumentBuilder().isNamespaceAware());
            dbf.setNamespaceAware(false);
            assertFalse(dbf.newDocumentBuilder().isNamespaceAware());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    public void testIsValidating() {
        try {
            dbf.setValidating(false);
            assertFalse(dbf.newDocumentBuilder().isValidating());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    public void testIsXIncludeAware() {
        try {
            dbf.setXIncludeAware(false);
            assertFalse(dbf.newDocumentBuilder().isXIncludeAware());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    /**
     * Tests that the Base URI for the document is populated with the file URI.
     */
    public void testGetBaseURI() throws IOException, SAXException {
        File f = Support_Resources.resourceToTempFile("/simple.xml");
        Document d = db.parse(f);
        assertTrue(d.getDocumentElement().getBaseURI().startsWith("file://"));
    }

    /**
     * javax.xml.parsers.DocumentBuilder#parse(java.io.File)
     * Case 1: Try to parse correct xml document.
     * Case 2: Try to call parse() with null argument.
     * Case 3: Try to parse a non-existent file.
     * Case 4: Try to parse incorrect xml file.
     */
    public void test_parseLjava_io_File() throws IOException {
        File f = Support_Resources.resourceToTempFile("/simple.xml");

        // case 1: Trivial use.
        try {
            Document d = db.parse(f);
            assertNotNull(d);
       //      TBD getXmlEncoding() IS NOT SUPPORTED
       //     assertEquals("ISO-8859-1", d.getXmlEncoding());
            assertEquals(2, d.getChildNodes().getLength());
            assertEquals("#comment",
                    d.getChildNodes().item(0).getNodeName());
            assertEquals("breakfast_menu",
                    d.getChildNodes().item(1).getNodeName());
        } catch (IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch (SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }

        // case 2: Try to call parse with null argument
        try {
            db.parse((File)null);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException iae) {
            // expected
        } catch (IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch (SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }

        // case 3: Try to parse a non-existent file
        try {
            db.parse(new File("_"));
            fail("Expected IOException was not thrown");
        } catch (IOException ioe) {
            // expected
        } catch (SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }

        // case 4: Try to parse incorrect xml file
        f = Support_Resources.resourceToTempFile("/wrong.xml");
        try {
            db.parse(f);
            fail("Expected SAXException was not thrown");
        } catch (IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch (SAXException sax) {
            // expected
        }
    }

    /**
     * javax.xml.parsers.DocumentBuilder#parse(java.io.InputStream)
     * Case 1: Try to parse correct xml document.
     * Case 2: Try to call parse() with null argument.
     * Case 3: Try to parse a non-existent file.
     * Case 4: Try to parse incorrect xml file.
     */
    public void test_parseLjava_io_InputStream() {
        InputStream is = getClass().getResourceAsStream("/simple.xml");
        // case 1: Trivial use.
        try {
            Document d = db.parse(is);
            assertNotNull(d);
            // TBD getXmlEncoding() IS NOT SUPPORTED
            // assertEquals("ISO-8859-1", d.getXmlEncoding());
            assertEquals(2, d.getChildNodes().getLength());
            assertEquals("#comment",
                    d.getChildNodes().item(0).getNodeName());
            assertEquals("breakfast_menu",
                    d.getChildNodes().item(1).getNodeName());
        } catch (IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch (SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }

        // case 2: Try to call parse with null argument
        try {
            db.parse((InputStream)null);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException iae) {
            // expected
        } catch (IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch (SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }

        // case 3: Try to parse a non-existent file
        try {
            db.parse(new FileInputStream("_"));
            fail("Expected IOException was not thrown");
        } catch (IOException ioe) {
            // expected
        } catch (SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }

        // case 4: Try to parse incorrect xml file
        try {
            is = getClass().getResourceAsStream("/wrong.xml");
            db.parse(is);
            fail("Expected SAXException was not thrown");
        } catch (IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch (SAXException sax) {
            // expected
        }
    }

    /**
     * javax.xml.parsers.DocumentBuilder#parse(java.io.InputStream)
     * Case 1: Try to parse correct xml document.
     * Case 2: Try to call parse() with null argument.
     * Case 3: Try to parse a non-existent file.
     * Case 4: Try to parse incorrect xml file.
     */
    public void testParseInputSource() {
        InputStream stream = getClass().getResourceAsStream("/simple.xml");
        InputSource is = new InputSource(stream);

        // case 1: Trivial use.
        try {
            Document d = db.parse(is);
            assertNotNull(d);
            // TBD getXmlEncoding() IS NOT SUPPORTED
            // assertEquals("ISO-8859-1", d.getXmlEncoding());
            assertEquals(2, d.getChildNodes().getLength());
            assertEquals("#comment",
                    d.getChildNodes().item(0).getNodeName());
            assertEquals("breakfast_menu",
                    d.getChildNodes().item(1).getNodeName());
        } catch (IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch (SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }

        // case 2: Try to call parse with null argument
        try {
            db.parse((InputSource)null);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException iae) {
            // expected
        } catch (IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch (SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }

        // case 3: Try to parse a non-existent file
        try {
            db.parse(new InputSource(new FileInputStream("_")));
            fail("Expected IOException was not thrown");
        } catch (IOException ioe) {
            // expected
        } catch (SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }

        // case 4: Try to parse incorrect xml file
        try {
            is = new InputSource(getClass().getResourceAsStream("/wrong.xml"));
            db.parse(is);
            fail("Expected SAXException was not thrown");
        } catch (IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch (SAXException sax) {
            // expected
        }
    }

    /**
     * javax.xml.parsers.DocumentBuilder#parse(java.io.InputStream,
     *     java.lang.String)
     * Case 1: Try to parse correct xml document.
     * Case 2: Try to call parse() with null argument.
     * Case 3: Try to parse a non-existent file.
     * Case 4: Try to parse incorrect xml file.
     */
    public void test_parseLjava_io_InputStreamLjava_lang_String() {
        InputStream is = getClass().getResourceAsStream("/systemid.xml");
        // case 1: Trivial use.
        try {
            Document d = db.parse(is, SAXParserTestSupport.XML_SYSTEM_ID);
            assertNotNull(d);
//           TBD getXmlEncoding() is not supported
//           assertEquals("UTF-8", d.getXmlEncoding());
            assertEquals(4, d.getChildNodes().getLength());
            assertEquals("collection",
                    d.getChildNodes().item(0).getNodeName());
            assertEquals("#comment",
                    d.getChildNodes().item(1).getNodeName());
            assertEquals("collection",
                    d.getChildNodes().item(2).getNodeName());
            assertEquals("#comment",
                    d.getChildNodes().item(3).getNodeName());
        } catch (IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch (SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }

        // case 2: Try to call parse with null argument
        try {
            db.parse((InputStream)null, SAXParserTestSupport.XML_SYSTEM_ID);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException iae) {
            // expected
        } catch (IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch (SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }

        // case 3: Try to parse a non-existent file
// Doesn't make sense this way...
//        try {
//            db.parse(is, "/");
//            fail("Expected IOException was not thrown");
//        } catch (IOException ioe) {
//            // expected
//        } catch (SAXException sax) {
//            fail("Unexpected SAXException " + sax.toString());
//        }

        // case 4: Try to parse incorrect xml file
        try {
            is = getClass().getResourceAsStream("/wrong.xml");
            db.parse(is, SAXParserTestSupport.XML_SYSTEM_ID);
            fail("Expected SAXException was not thrown");
        } catch (IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch (SAXException sax) {
            // expected
        }
    }

    /**
     * javax.xml.parsers.DocumentBuilder#parse(java.lang.String)
     * Case 1: Try to parse correct xml document.
     * Case 2: Try to call parse() with null argument.
     * Case 3: Try to parse a non-existent uri.
     * Case 4: Try to parse incorrect xml file.
     */
    public void test_parseLjava_lang_String() throws Exception {
        // case 1: Trivial use.
        URL resource = getClass().getResource("/simple.xml");
        Document d = db.parse(resource.toString());
        assertNotNull(d);
//          TBD  getXmlEncoding() is not supported
//          assertEquals("ISO-8859-1", d.getXmlEncoding());
        assertEquals(2, d.getChildNodes().getLength());
        assertEquals("#comment",
                d.getChildNodes().item(0).getNodeName());
        assertEquals("breakfast_menu",
                d.getChildNodes().item(1).getNodeName());

        // case 2: Try to call parse with null argument
        try {
            db.parse((String)null);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException iae) {
            // expected
        }

        // case 3: Try to parse a non-existent uri
        try {
            db.parse("_");
            fail("Expected IOException was not thrown");
        } catch (IOException ioe) {
            // expected
        }

        // case 4: Try to parse incorrect xml file
        try {
            resource = getClass().getResource("/wrong.xml");
            db.parse(resource.toString());
            fail("Expected SAXException was not thrown");
        } catch (SAXException sax) {
            // expected
        }
    }

    public void testReset() {
        // Make sure EntityResolver gets reset
        InputStream source = new ByteArrayInputStream("<a>&foo;</a>".getBytes());
        InputStream entity = new ByteArrayInputStream("bar".getBytes());

        MockResolver resolver = new MockResolver();
        resolver.addEntity("foo", "foo", new InputSource(entity));

        Document d;

        try {
            db = dbf.newDocumentBuilder();
            db.setEntityResolver(resolver);
            db.reset();
            d = db.parse(source);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        Element root = (Element)d.getElementsByTagName("a").item(0);
        assertEquals("foo", ((EntityReference)root.getFirstChild()).getNodeName());

        // Make sure ErrorHandler gets reset
        source = new ByteArrayInputStream("</a>".getBytes());

        MethodLogger logger = new MethodLogger();
        ErrorHandler handler = new MockHandler(logger);

        try {
            db = dbf.newDocumentBuilder();
            db.setErrorHandler(handler);
            db.reset();
            d = db.parse(source);
        } catch (SAXParseException e) {
            // Expected
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        assertEquals(0, logger.size());
    }

    public void testSetErrorHandler() {
        // Ordinary case
        InputStream source = new ByteArrayInputStream("</a>".getBytes());

        MethodLogger logger = new MethodLogger();
        ErrorHandler handler = new MockHandler(logger);

        try {
            db = dbf.newDocumentBuilder();
            db.setErrorHandler(handler);
            db.parse(source);
        } catch (SAXParseException e) {
            // Expected, ErrorHandler does not mask exception
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        assertEquals("error", logger.getMethod());
        assertTrue(logger.getArgs()[0] instanceof SAXParseException);

        // null case
        source = new ByteArrayInputStream("</a>".getBytes());

        try {
            db = dbf.newDocumentBuilder();
            db.setErrorHandler(null);
            db.parse(source);
        } catch (SAXParseException e) {
            // Expected
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    public void testSetEntityResolver() {
        // Ordinary case
        InputStream source = new ByteArrayInputStream("<a>&foo;</a>".getBytes());
        InputStream entity = new ByteArrayInputStream("bar".getBytes());

        MockResolver resolver = new MockResolver();
        resolver.addEntity("foo", "foo", new InputSource(entity));

        Document d;

        try {
            db = dbf.newDocumentBuilder();
            db.setEntityResolver(resolver);
            d = db.parse(source);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        Element root = (Element)d.getElementsByTagName("a").item(0);
        assertEquals("bar", ((Text)root.getFirstChild()).getData());

        // null case
        source = new ByteArrayInputStream("<a>&foo;</a>".getBytes());

        try {
            db = dbf.newDocumentBuilder();
            db.setEntityResolver(null);
            d = db.parse(source);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        root = (Element)d.getElementsByTagName("a").item(0);
        assertEquals("foo", ((EntityReference)root.getFirstChild()).getNodeName());
    }

}
