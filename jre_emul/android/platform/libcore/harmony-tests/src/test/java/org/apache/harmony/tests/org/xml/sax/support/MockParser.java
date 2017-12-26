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
import java.util.Locale;

import org.xml.sax.DTDHandler;
import org.xml.sax.DocumentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;

@SuppressWarnings("deprecation")
public class MockParser implements Parser {

    private MethodLogger logger;

    public MockParser(MethodLogger logger) {
        super();
        this.logger = logger;
    }

    public void parse(InputSource source) throws SAXException, IOException {
        logger.add("parse", source);
    }

    public void parse(String systemId) throws SAXException, IOException {
        logger.add("parse", systemId);
    }

    public void setDTDHandler(DTDHandler handler) {
        logger.add("setDTDHandler", handler);
    }

    public void setDocumentHandler(DocumentHandler handler) {
        logger.add("setDocumentHandler", handler);
    }

    public void setEntityResolver(EntityResolver resolver) {
        logger.add("setEntityResolver", resolver);
    }

    public void setErrorHandler(ErrorHandler handler) {
        logger.add("setErrorHandler", handler);
    }

    public void setLocale(Locale locale) throws SAXException {
        logger.add("setLocale", locale);
    }

}
