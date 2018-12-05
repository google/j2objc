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
package org.apache.harmony.tests.javax.xml.parsers;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

public class DocumentBuilderFactoryTest extends TestCase {

    DocumentBuilderFactory dbf;

    List<String> cdataElements;

    List<String> textElements;

    List<String> commentElements;

    protected void setUp() throws Exception {
        super.setUp();
        dbf = DocumentBuilderFactory.newInstance();

        cdataElements = new ArrayList<String>();
        textElements = new ArrayList<String>();
        commentElements = new ArrayList<String>();
    }

    protected void tearDown() throws Exception {
        dbf = null;
        cdataElements = null;
        textElements = null;
        commentElements = null;
        super.tearDown();
    }

    /**
     * javax.xml.parsers.DocumentBuilderFactory#DocumentBuilderFactory().
     */
    public void test_Constructor() {
        try {
            new DocumentBuilderFactoryChild();
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }

    /**
     * javax.xml.parsers.DocumentBuilderFactory#getAttribute(String).
     */
//    public void test_getAttributeLjava_lang_String() {
//        String[] attributes = {
//                "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
//                "http://java.sun.com/xml/jaxp/properties/schemaSource" };
//        Object[] values = { "http://www.w3.org/2001/XMLSchema", "source" };
//
//        try {
//            for (int i = 0; i < attributes.length; i++) {
//                dbf.setAttribute(attributes[i], values[i]);
//                assertEquals(values[i], dbf.getAttribute(attributes[i]));
//            }
//        } catch (IllegalArgumentException e) {
//            fail("Unexpected IllegalArgumentException" + e.getMessage());
//        } catch (Exception e) {
//            fail("Unexpected exception" + e.getMessage());
//        }
//
//        try {
//            for (int i = 0; i < attributes.length; i++) {
//                dbf.setAttribute(null, null);
//                fail("NullPointerException expected");
//            }
//        } catch (NullPointerException e) {
//            // expected
//        }
//
//        String[] badAttributes = {"bad1", "bad2", ""};
//        try {
//            for (int i = 0; i < badAttributes.length; i++) {
//                dbf.getAttribute(badAttributes[i]);
//                fail("IllegalArgumentException expected");
//            }
//        } catch (IllegalArgumentException e) {
//            // expected
//        }
//    }

    /**
     * javax.xml.parsers.DocumentBuilderFactory#getFeature(String).
     */
// TODO Fails on JDK. Why?
//    public void test_getFeatureLjava_lang_String() {
//        String[] features = { "http://xml.org/sax/features/namespaces",
//                "http://xml.org/sax/features/validation",
//                "http://xml.org/sax/features/external-general-entities" };
//        try {
//            for (int i = 0; i < features.length; i++) {
//                dbf.setFeature(features[i], true);
//                assertTrue(dbf.getFeature(features[i]));
//            }
//        } catch (ParserConfigurationException e) {
//            fail("Unexpected ParserConfigurationException " + e.getMessage());
//        }
//
//        try {
//            for (int i = 0; i < features.length; i++) {
//                dbf.setFeature(features[i], false);
//                assertFalse(dbf.getFeature(features[i]));
//            }
//        } catch (ParserConfigurationException e) {
//            fail("Unexpected ParserConfigurationException " + e.getMessage());
//        }
//
//        try {
//            for (int i = 0; i < features.length; i++) {
//                dbf.setFeature(null, false);
//                fail("NullPointerException expected");
//            }
//        } catch (NullPointerException e) {
//            // expected
//        } catch (ParserConfigurationException e) {
//            fail("Unexpected ParserConfigurationException" + e.getMessage());
//        }
//
//        String[] badFeatures = {"bad1", "bad2", ""};
//        try {
//            for (int i = 0; i < badFeatures.length; i++) {
//                dbf.getFeature(badFeatures[i]);
//                fail("ParserConfigurationException expected");
//            }
//        } catch (ParserConfigurationException e) {
//            // expected
//        }
//
//    }

    /**
     * javax.xml.parsers.DocumentBuilderFactory#getSchema().
     *  TBD getSchemas() IS NOT SUPPORTED
     */
/*    public void test_getSchema() {
        assertNull(dbf.getSchema());
        SchemaFactory sf =
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            Schema schema = sf.newSchema();
            dbf.setSchema(schema);
            assertNotNull(dbf.getSchema());
        } catch (SAXException sax) {
            fail("Unexpected exception " + sax.toString());
        }
    }
    */

    /**
     * javax.xml.parsers.DocumentBuilderFactory#isCoalescing().
     */
    public void test_isCoalescing() {
        dbf.setCoalescing(true);
        assertTrue(dbf.isCoalescing());

        dbf.setCoalescing(false);
        assertFalse(dbf.isCoalescing());
    }

    /**
     * javax.xml.parsers.DocumentBuilderFactory#isExpandEntityReferences().
     */
    public void test_isExpandEntityReferences() {
        dbf.setExpandEntityReferences(true);
        assertTrue(dbf.isExpandEntityReferences());

        dbf.setExpandEntityReferences(false);
        assertFalse(dbf.isExpandEntityReferences());
    }

    /**
     * javax.xml.parsers.DocumentBuilderFactory#isIgnoringComments().
     */
    public void test_isIgnoringComments() {
        dbf.setIgnoringComments(true);
        assertTrue(dbf.isIgnoringComments());

        dbf.setIgnoringComments(false);
        assertFalse(dbf.isIgnoringComments());
    }

    /**
     * javax.xml.parsers.DocumentBuilderFactory#isIgnoringElementContentWhitespace().
     */
    public void test_isIgnoringElementContentWhitespace() {
        dbf.setIgnoringElementContentWhitespace(true);
        assertTrue(dbf.isIgnoringElementContentWhitespace());

        dbf.setIgnoringElementContentWhitespace(false);
        assertFalse(dbf.isIgnoringElementContentWhitespace());
    }

    /**
     * javax.xml.parsers.DocumentBuilderFactory#isNamespaceAware().
     */
    public void test_isNamespaceAware() {
        dbf.setNamespaceAware(true);
        assertTrue(dbf.isNamespaceAware());

        dbf.setNamespaceAware(false);
        assertFalse(dbf.isNamespaceAware());
    }

    public void test_setIsValidating() {
        dbf.setValidating(true);
        assertTrue(dbf.isValidating());

        dbf.setValidating(false);
        assertFalse(dbf.isValidating());
    }

/*    public void test_isSetXIncludeAware() {
        dbf.setXIncludeAware(true);
        assertTrue(dbf.isXIncludeAware());

        dbf.setXIncludeAware(false);
        assertFalse(dbf.isXIncludeAware());
    }
*/

    /**
     * javax.xml.parsers.DocumentBuilderFactory#newInstance().
     */
    public void test_newInstance() {
        String className = null;
        try {
            // case 1: Try to obtain a new instance of factory by default.
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            assertNotNull(dbf);

            // case 2: Try to create a new instance of factory using
            // property DATATYPEFACTORY_PROPERTY
            className = System.getProperty("javax.xml.parsers.DocumentBuilderFactory");
            System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
                    "org.apache.harmony.xml.parsers.DocumentBuilderFactoryImpl");

            dbf = DocumentBuilderFactory.newInstance();
            assertNotNull(dbf);
            assertTrue(dbf instanceof org.apache.harmony.xml.parsers.DocumentBuilderFactoryImpl);

            // case 3: Try to create a new instance of factory using Property
            String keyValuePair = "javax.xml.parsers.DocumentBuilderFactory"
                    + "=" + "org.apache.harmony.xml.parsers.DocumentBuilderFactoryImpl";
            ByteArrayInputStream bis = new ByteArrayInputStream(keyValuePair
                    .getBytes());
            Properties prop = System.getProperties();
            prop.load(bis);
            dbf = DocumentBuilderFactory.newInstance();
            assertNotNull(dbf);
            assertTrue(dbf instanceof org.apache.harmony.xml.parsers.DocumentBuilderFactoryImpl);

            // case 4: Check FactoryConfiguration error
            System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "");
            try {
                DocumentBuilderFactory.newInstance();
            } catch (FactoryConfigurationError fce) {
                // expected
            }

        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        } finally {
            // Set default value of Datatype factory,
            // because of this test modifies it.
            if (className == null) {
                System.clearProperty("javax.xml.parsers.DocumentBuilderFactory");
            } else {
                System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
                    className);
            }
        }
    }

    public void test_newDocumentBuilder() {
        // Ordinary case
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            assertTrue(db instanceof DocumentBuilder);
            db.parse(getClass().getResourceAsStream("/simple.xml"));
        } catch(Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        // Exception case
        dbf.setValidating(true);
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
        } catch(ParserConfigurationException e) {
            // Expected, since Android doesn't have a validating parser.
        }
    }

    /**
     * javax.xml.parsers.DocumentBuilderFactory#setAttribute(java.lang.String,
     *     java.lang.Object).
     */
//    public void test_setAttributeLjava_lang_StringLjava_lang_Object() {
//        String[] attributes = {
//                "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
//                "http://java.sun.com/xml/jaxp/properties/schemaSource" };
//        Object[] values = { "http://www.w3.org/2001/XMLSchema", "source" };
//
//        try {
//            for (int i = 0; i < attributes.length; i++) {
//                dbf.setAttribute(attributes[i], values[i]);
//                assertEquals(values[i], dbf.getAttribute(attributes[i]));
//            }
//        } catch (IllegalArgumentException e) {
//            fail("Unexpected IllegalArgumentException" + e.getMessage());
//        } catch (Exception e) {
//            fail("Unexpected exception" + e.getMessage());
//        }
//
//        String[] badAttributes = {"bad1", "bad2", ""};
//        try {
//            for (int i = 0; i < badAttributes.length; i++) {
//                dbf.setAttribute(badAttributes[i], "");
//                fail("IllegalArgumentException expected");
//            }
//        } catch (IllegalArgumentException iae) {
//            // expected
//        }
//
//        try {
//            for (int i = 0; i < attributes.length; i++) {
//                dbf.setAttribute(null, null);
//                fail("NullPointerException expected");
//            }
//        } catch (NullPointerException e) {
//            // expected
//        }
//    }

    /**
     * javax.xml.parsers.DocumentBuilderFactory#setCoalescing(boolean).
     */
    public void test_setCoalescingZ() {
        dbf.setCoalescing(true);
        assertTrue(dbf.isCoalescing());

        textElements.clear();
        cdataElements.clear();
        Exception parseException = null;
        DocumentBuilder parser = null;

        try {
            parser = dbf.newDocumentBuilder();
            ValidationErrorHandler errorHandler = new ValidationErrorHandler();
            parser.setErrorHandler(errorHandler);

            Document document = parser.parse(getClass().getResourceAsStream(
                    "/recipt.xml"));

            parseException = errorHandler.getFirstException();

            goThroughDocument((Node) document, "");
            assertTrue(textElements
                    .contains("BeefParmesan<title>withGarlicAngelHairPasta</title>"));
        } catch (Exception ex) {
            parseException = ex;
        }
        parser.setErrorHandler(null);

        if (parseException != null) {
            fail("Unexpected exception " + parseException.getMessage());
        }

        dbf.setCoalescing(false);
        assertFalse(dbf.isCoalescing());

        textElements.clear();
        cdataElements.clear();

        try {
            parser = dbf.newDocumentBuilder();
            ValidationErrorHandler errorHandler = new ValidationErrorHandler();
            parser.setErrorHandler(errorHandler);

            Document document = parser.parse(getClass().getResourceAsStream(
                    "/recipt.xml"));

            parseException = errorHandler.getFirstException();

            goThroughDocument((Node) document, "");

            assertFalse(textElements
                    .contains("BeefParmesan<title>withGarlicAngelHairPasta</title>"));

        } catch (Exception ex) {
            parseException = ex;
        }
        parser.setErrorHandler(null);

        if (parseException != null) {
            fail("Unexpected exception " + parseException.getMessage());
        }
    }

    /**
     * javax.xml.parsers.DocumentBuilderFactory#setExpandEntityReferences(boolean).
     */
    public void test_setExpandEntityReferencesZ() {
        dbf.setExpandEntityReferences(true);
        assertTrue(dbf.isExpandEntityReferences());

        Exception parseException = null;
        DocumentBuilder parser = null;

        try {
            parser = dbf.newDocumentBuilder();
            ValidationErrorHandler errorHandler = new ValidationErrorHandler();
            parser.setErrorHandler(errorHandler);

            Document document = parser.parse(getClass().getResourceAsStream(
                    "/recipt.xml"));

            parseException = errorHandler.getFirstException();

            assertNotNull(document);

        } catch (Exception ex) {
            parseException = ex;
        }
        parser.setErrorHandler(null);

        if (parseException != null) {
            fail("Unexpected exception " + parseException.getMessage());
        }

        dbf.setExpandEntityReferences(false);
        assertFalse(dbf.isExpandEntityReferences());
        try {
            parser = dbf.newDocumentBuilder();
            ValidationErrorHandler errorHandler = new ValidationErrorHandler();
            parser.setErrorHandler(errorHandler);

            Document document = parser.parse(getClass().getResourceAsStream(
                    "/recipt.xml"));

            parseException = errorHandler.getFirstException();

            assertNotNull(document);

        } catch (Exception ex) {
            parseException = ex;
        }
        parser.setErrorHandler(null);

        if (parseException != null) {
            fail("Unexpected exception " + parseException.getMessage());
        }
    }

    /**
     * javax.xml.parsers.DocumentBuilderFactory#setFeature(java.lang.String).
     */
    public void test_getSetFeatureLjava_lang_String() {
        String[] features = { "http://xml.org/sax/features/namespaces",
                "http://xml.org/sax/features/validation" };
        try {
            for (int i = 0; i < features.length; i++) {
                dbf.setFeature(features[i], true);
                assertTrue(dbf.getFeature(features[i]));
            }
        } catch (ParserConfigurationException e) {
            fail("Unexpected ParserConfigurationException" + e.getMessage());
        }

        try {
            for (int i = 0; i < features.length; i++) {
                dbf.setFeature(features[i], false);
                assertFalse(dbf.getFeature(features[i]));
            }
        } catch (ParserConfigurationException e) {
            fail("Unexpected ParserConfigurationException" + e.getMessage());
        }

        try {
            for (int i = 0; i < features.length; i++) {
                dbf.setFeature(null, false);
                fail("NullPointerException expected");
            }
        } catch (NullPointerException e) {
            // expected
        } catch (ParserConfigurationException e) {
            fail("Unexpected ParserConfigurationException" + e.getMessage());
        }

        String[] badFeatures = { "bad1", "bad2", "" };
        try {
            for (int i = 0; i < badFeatures.length; i++) {
                dbf.setFeature(badFeatures[i], false);
                fail("ParserConfigurationException expected");
            }
        } catch (ParserConfigurationException e) {
            // expected
        }
    }

    /**
     * javax.xml.parsers.DocumentBuilderFactory#setIgnoringComments(boolean).
     */
    public void test_setIgnoringCommentsZ() {
        commentElements.clear();

        dbf.setIgnoringComments(true);
        assertTrue(dbf.isIgnoringComments());

        try {
            DocumentBuilder parser = dbf.newDocumentBuilder();

            Document document = parser.parse(getClass().getResourceAsStream(
                    "/recipt.xml"));

            goThroughDocument((Node) document, "");
            assertFalse(commentElements.contains("comment1"));
            assertFalse(commentElements.contains("comment2"));

        } catch (IOException e) {
            fail("Unexpected IOException " + e.getMessage());
        } catch (ParserConfigurationException e) {
            fail("Unexpected ParserConfigurationException " + e.getMessage());
        } catch (SAXException e) {
            fail("Unexpected SAXException " + e.getMessage());
        }

        commentElements.clear();

        dbf.setIgnoringComments(false);
        assertFalse(dbf.isIgnoringComments());

        try {
            DocumentBuilder parser = dbf.newDocumentBuilder();

            Document document = parser.parse(getClass().getResourceAsStream(
                    "/recipt.xml"));

            goThroughDocument((Node) document, "");
            assertTrue(commentElements.contains("comment1"));
            assertTrue(commentElements.contains("comment2"));

        } catch (IOException e) {
            fail("Unexpected IOException " + e.getMessage());
        } catch (ParserConfigurationException e) {
            fail("Unexpected ParserConfigurationException " + e.getMessage());
        } catch (SAXException e) {
            fail("Unexpected SAXException " + e.getMessage());
        }
    }

    /**
     * javax.xml.parsers.DocumentBuilderFactory#setIgnoringElementContentWhitespace(boolean).
     */
    public void test_setIgnoringElementContentWhitespaceZ() {
        dbf.setIgnoringElementContentWhitespace(true);
        assertTrue(dbf.isIgnoringElementContentWhitespace());

        try {
            DocumentBuilder parser = dbf.newDocumentBuilder();

            Document document = parser.parse(getClass().getResourceAsStream(
                    "/recipt.xml"));

            assertNotNull(document);

        } catch (IOException e) {
            fail("Unexpected IOException " + e.getMessage());
        } catch (ParserConfigurationException e) {
            fail("Unexpected ParserConfigurationException " + e.getMessage());
        } catch (SAXException e) {
            fail("Unexpected SAXException " + e.getMessage());
        }

        dbf.setIgnoringElementContentWhitespace(false);
        assertFalse(dbf.isIgnoringElementContentWhitespace());

        try {
            DocumentBuilder parser = dbf.newDocumentBuilder();

            Document document = parser.parse(getClass().getResourceAsStream(
                    "/recipt.xml"));

            assertNotNull(document);

        } catch (IOException e) {
            fail("Unexpected IOException " + e.getMessage());
        } catch (ParserConfigurationException e) {
            fail("Unexpected ParserConfigurationException " + e.getMessage());
        } catch (SAXException e) {
            fail("Unexpected SAXException " + e.getMessage());
        }
    }

    /**
     * javax.xml.parsers.DocumentBuilderFactory#setNamespaceAware(boolean).
     */
    public void test_setNamespaceAwareZ() {
        dbf.setNamespaceAware(true);
        assertTrue(dbf.isNamespaceAware());

        try {
            DocumentBuilder parser = dbf.newDocumentBuilder();

            Document document = parser.parse(getClass().getResourceAsStream(
                    "/recipt.xml"));

            assertNotNull(document);

        } catch (IOException e) {
            fail("Unexpected IOException " + e.getMessage());
        } catch (ParserConfigurationException e) {
            fail("Unexpected ParserConfigurationException " + e.getMessage());
        } catch (SAXException e) {
            fail("Unexpected SAXException " + e.getMessage());
        }

        dbf.setNamespaceAware(false);
        assertFalse(dbf.isNamespaceAware());

        try {
            DocumentBuilder parser = dbf.newDocumentBuilder();

            Document document = parser.parse(getClass().getResourceAsStream(
                    "/recipt.xml"));

            assertNotNull(document);

        } catch (IOException e) {
            fail("Unexpected IOException " + e.getMessage());
        } catch (ParserConfigurationException e) {
            fail("Unexpected ParserConfigurationException " + e.getMessage());
        } catch (SAXException e) {
            fail("Unexpected SAXException " + e.getMessage());
        }
    }

    public void test_getSetAttribute() {
        // Android SAX implementation doesn't support attributes, so
        // we can only make sure the expected exception is thrown.
        try {
            dbf.setAttribute("foo", new Object());
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        try {
            dbf.getAttribute("foo");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    /**
     * javax.xml.parsers.DocumentBuilderFactory#setSchema(javax.xml.validation.Schema).
     */
 /*   public void test_setSchemaLjavax_xml_validation_Schema() {
        SchemaFactory sf =
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            Schema schema = sf.newSchema();
            dbf.setSchema(schema);
            assertNotNull(dbf.getSchema());
        } catch (SAXException sax) {
            fail("Unexpected exception " + sax.toString());
        }
    }
*/
    /**
     * javax.xml.parsers.DocumentBuilderFactory#setValidating(boolean).
     */
//    public void test_setValidatingZ() {
//        Exception parseException = null;
//        DocumentBuilder parser = null;
//        Document document = null;
//
//        ValidationErrorHandler errorHandler = new ValidationErrorHandler();
//
//        dbf.setValidating(false);
//        assertFalse(dbf.isValidating());
//
//        // case 1: Validation is not set. Correct xml-file
//        try {
//
//            parser = dbf.newDocumentBuilder();
//            parser.setErrorHandler(errorHandler);
//
//            document = parser.parse(getClass().getResourceAsStream(
//                    "/recipt.xml"));
//
//            parseException = errorHandler.getFirstException();
//
//            assertNotNull(document);
//
//            document = parser.parse(getClass().getResourceAsStream(
//                    "/reciptWrong.xml"));
//
//            parseException = errorHandler.getFirstException();
//
//            assertNotNull(document);
//
//        } catch (Exception ex) {
//            parseException = ex;
//        }
//        parser.setErrorHandler(null);
//
//        if (parseException != null) {
//            fail("Unexpected exception " + parseException.getMessage());
//        }
//
//        // case 2: Validation is not set. Wrong xml-file
//        try {
//
//            parser = dbf.newDocumentBuilder();
//            parser.setErrorHandler(errorHandler);
//
//            document = parser.parse(getClass().getResourceAsStream(
//                    "/reciptWrong.xml"));
//            parseException = errorHandler.getFirstException();
//
//            assertNotNull(document);
//
//        } catch (Exception ex) {
//            parseException = ex;
//        }
//        parser.setErrorHandler(null);
//
//        if (parseException != null) {
//            fail("Unexpected exception " + parseException.getMessage());
//        }
//
//        // case 3: Validation is set. Correct xml-file
//        dbf.setValidating(true);
//        assertTrue(dbf.isValidating());
//
//        try {
//
//            parser = dbf.newDocumentBuilder();
//            parser.setErrorHandler(errorHandler);
//
//            document = parser.parse(getClass().getResourceAsStream(
//                    "/recipt.xml"));
//            parseException = errorHandler.getFirstException();
//
//            assertNotNull(document);
//
//        } catch (Exception ex) {
//            parseException = ex;
//        }
//        parser.setErrorHandler(null);
//
//        if (parseException != null) {
//            fail("Unexpected exception " + parseException.getMessage());
//        }
//
//        // case 4: Validation is set. Wrong xml-file
//        try {
//
//            parser = dbf.newDocumentBuilder();
//            parser.setErrorHandler(errorHandler);
//
//            document = parser.parse(getClass().getResourceAsStream(
//                    "/reciptWrong.xml"));
//            parseException = errorHandler.getFirstException();
//
//            assertNotNull(document);
//
//        } catch (Exception ex) {
//            parseException = ex;
//        }
//        parser.setErrorHandler(null);
//
//        if (parseException == null) {
//            fail("Unexpected exception " + parseException.getMessage());
//        } else {
//            assertTrue(parseException
//                    .getMessage()
//                    .contains(
//                            "The content of element type \"collection\" must match \"(description,recipe+)\""));
//        }
//
//    }

    /**
     * javax.xml.parsers.DocumentBuilderFactory#setXIncludeAware().
     */
//    public void test_setXIncludeAware() {
//        dbf.setXIncludeAware(true);
//        assertTrue(dbf.isXIncludeAware());
//
//        try {
//            DocumentBuilder parser = dbf.newDocumentBuilder();
//
//            Document document = parser.parse(getClass().getResourceAsStream(
//                    "/recipt.xml"));
//
//            assertNotNull(document);
//
//        } catch (IOException e) {
//            fail("Unexpected IOException " + e.getMessage());
//        } catch (ParserConfigurationException e) {
//            fail("Unexpected ParserConfigurationException " + e.getMessage());
//        } catch (SAXException e) {
//            fail("Unexpected SAXException " + e.getMessage());
//        }
//
//        dbf.setXIncludeAware(false);
//        assertFalse(dbf.isXIncludeAware());
//
//        try {
//            DocumentBuilder parser = dbf.newDocumentBuilder();
//
//            Document document = parser.parse(getClass().getResourceAsStream(
//                    "/recipt.xml"));
//
//            assertNotNull(document);
//
//        } catch (IOException e) {
//            fail("Unexpected IOException " + e.getMessage());
//        } catch (ParserConfigurationException e) {
//            fail("Unexpected ParserConfigurationException " + e.getMessage());
//        } catch (SAXException e) {
//            fail("Unexpected SAXException " + e.getMessage());
//        }
//    }

    private void goThroughDocument(Node node, String indent) {
        String value = node.getNodeValue();

        if (value != null) {
            value = value.replaceAll(" ", "");
            value = value.replaceAll("\n", "");
        }

        switch (node.getNodeType()) {
        case Node.CDATA_SECTION_NODE:
            cdataElements.add(value);
            // System.out.println(indent + "CDATA_SECTION_NODE " + value);
            break;
        case Node.COMMENT_NODE:
            commentElements.add(value);
            // System.out.println(indent + "COMMENT_NODE " + value);
            break;
        case Node.DOCUMENT_FRAGMENT_NODE:
            // System.out.println(indent + "DOCUMENT_FRAGMENT_NODE " + value);
            break;
        case Node.DOCUMENT_NODE:
            // System.out.println(indent + "DOCUMENT_NODE " + value);
            break;
        case Node.DOCUMENT_TYPE_NODE:
            // System.out.println(indent + "DOCUMENT_TYPE_NODE " + value);
            break;
        case Node.ELEMENT_NODE:
            // System.out.println(indent + "ELEMENT_NODE " + value);
            break;
        case Node.ENTITY_NODE:
            // System.out.println(indent + "ENTITY_NODE " + value);
            break;
        case Node.ENTITY_REFERENCE_NODE:
            // System.out.println(indent + "ENTITY_REFERENCE_NODE " + value);
            break;
        case Node.NOTATION_NODE:
            // System.out.println(indent + "NOTATION_NODE " + value);
            break;
        case Node.PROCESSING_INSTRUCTION_NODE:
            // System.out.println(indent + "PROCESSING_INSTRUCTION_NODE " +
            // value);
            break;
        case Node.TEXT_NODE:
            textElements.add(value);
            // System.out.println(indent + "TEXT_NODE " + value);
            break;
        default:
            // System.out.println(indent + "Unknown node " + value);
            break;
        }
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++)
            goThroughDocument(list.item(i), indent + "   ");
    }

    private class ValidationErrorHandler implements ErrorHandler {
        private SAXException parseException;

        private int errorCount;

        private int warningCount;

        public ValidationErrorHandler() {
            parseException = null;
            errorCount = 0;
            warningCount = 0;
        }

        public void error(SAXParseException ex) {
            errorCount++;
            if (parseException == null) {
                parseException = ex;
            }
        }

        public void warning(SAXParseException ex) {
            warningCount++;
        }

        public void fatalError(SAXParseException ex) {
            if (parseException == null) {
                parseException = ex;
            }
        }

        public SAXException getFirstException() {
            return parseException;
        }
    }

    private class DocumentBuilderFactoryChild extends DocumentBuilderFactory {
        public DocumentBuilderFactoryChild() {
            super();
        }

        public Object getAttribute(String name) {
            return null;
        }

        public boolean getFeature(String name) {
            return false;
        }

        public DocumentBuilder newDocumentBuilder() {
            return null;
        }

        public void setAttribute(String name, Object value) {
        }

        public void setFeature(String name, boolean value) {
        }

    }
}
