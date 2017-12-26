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

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/**
 * A helper class that implements the SAX XMLReader interface and logs method
 * calls.
 */
public class MockReader implements XMLReader {

    private MethodLogger logger;

    private ContentHandler contentHandler;

    private DTDHandler dtdHandler;

    private EntityResolver resolver;

    private ErrorHandler errorHandler;

    private Set<String> features = new HashSet<String>();

    private Map<String, Object> properties = new HashMap<String, Object>();

    public MockReader(MethodLogger logger) {
        super();
        this.logger = logger;
    }


    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    public DTDHandler getDTDHandler() {
        return dtdHandler;
    }

    public EntityResolver getEntityResolver() {
        return resolver;
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public boolean getFeature(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        return features.contains(name);
    }

    public Object getProperty(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        return properties.get(name);
    }

    public void parse(InputSource input) throws IOException, SAXException {
        logger.add("parse", input);
    }

    public void parse(String systemId) throws IOException, SAXException {
        logger.add("parse", systemId);
    }

    public void setContentHandler(ContentHandler handler) {
        this.contentHandler = handler;
    }

    public void setDTDHandler(DTDHandler handler) {
        this.dtdHandler = handler;
    }

    public void setEntityResolver(EntityResolver resolver) {
        this.resolver = resolver;
    }

    public void setErrorHandler(ErrorHandler handler) {
        this.errorHandler = handler;
    }

    public void setFeature(String name, boolean value) {
        if (value) {
            features.add(name);
        } else {
            features.remove(name);
        }
    }

    public void setProperty(String name, Object value) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        if (value == null) {
            properties.remove(name);
        } else {
            properties.put(name, value);
        }
    }

}
