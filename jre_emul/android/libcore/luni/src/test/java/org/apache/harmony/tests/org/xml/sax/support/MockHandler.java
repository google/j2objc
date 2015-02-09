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

package org.apache.harmony.tests.org.xml.sax.support;

import org.xml.sax.AttributeList;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.DocumentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.LexicalHandler;

/**
 * A helper class that implements the various SAX callback interfaces and logs
 * method calls.
 */
@SuppressWarnings("deprecation")
public class MockHandler implements ContentHandler, DTDHandler, DocumentHandler,
        ErrorHandler, LexicalHandler {

    private MethodLogger logger;

    public MockHandler(MethodLogger logger) {
        super();
        this.logger = logger;
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        logger.add("characters", ch, start, length);
    }

    public void endDocument() throws SAXException {
        logger.add("endDocument");
    }

    public void endElement(String name) throws SAXException {
        logger.add("endElement", name);
    }

    public void endElement(String uri, String localName, String name) throws SAXException {
        logger.add("endElement", uri, localName, name);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        logger.add("endPrefixMapping", prefix);
    }

    public void error(SAXParseException exception) throws SAXException {
        logger.add("error", exception);
    }

    public void fatalError(SAXParseException exception) throws SAXException {
        logger.add("fatalError", exception);
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        logger.add("ignorableWhitespace", ch, start, length);
    }

    public void notationDecl(String name, String publicId, String systemId) throws SAXException {
        logger.add("notationDecl", name, publicId, systemId);
    }

    public void processingInstruction(String target, String data) throws SAXException {
        logger.add("processingInstruction", target, data);
    }

    public void setDocumentLocator(Locator locator) {
        logger.add("setDocumentLocator", locator);
    }

    public void skippedEntity(String name) throws SAXException {
        logger.add("skippedEntity", name);
    }

    public void startDocument() throws SAXException {
        logger.add("startDocument");
    }

    public void startElement(String name, AttributeList atts) throws SAXException {
        logger.add("startElement", name, atts);
    }

    public void startElement(String uri, String localName, String name, Attributes atts)
            throws SAXException {
        logger.add("startElement", uri, localName, name, atts);
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        logger.add("startPrefixMapping", prefix, uri);
    }

    public void unparsedEntityDecl(String name, String publicId, String systemId,
            String notationName) throws SAXException {
        logger.add("unparsedEntityDecl", name, publicId, systemId, notationName);
    }

    public void warning(SAXParseException exception) throws SAXException {
        logger.add("warning", exception);
    }

    public void comment(char[] ch, int start, int length) throws SAXException {
        logger.add("comment", ch, start, length);
    }

    public void endCDATA() throws SAXException {
        logger.add("endCDATA");
    }

    public void endDTD() throws SAXException {
        logger.add("endDTD");
    }

    public void endEntity(String name) throws SAXException {
        logger.add("endEntity", name);
    }

    public void startCDATA() throws SAXException {
        logger.add("startCDATA");
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        logger.add("startDTD", name, publicId, systemId);
    }

    public void startEntity(String name) throws SAXException {
        logger.add("startEntity", name);
    }

}
