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

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * An XMLReader that does nothing, but can be instantiated properly.
 */
public class DoNothingXMLReader implements XMLReader {

    public ContentHandler getContentHandler() {
        return null;
    }

    public DTDHandler getDTDHandler() {
        return null;
    }

    public EntityResolver getEntityResolver() {
        return null;
    }

    public ErrorHandler getErrorHandler() {
        return null;
    }

    public boolean getFeature(String name) {
        return false;
    }

    public Object getProperty(String name) {
        return null;
    }

    public void parse(InputSource input) {
    }

    public void parse(String systemId) {
    }

    public void setContentHandler(ContentHandler handler) {
    }

    public void setDTDHandler(DTDHandler handler) {
    }

    public void setEntityResolver(EntityResolver resolver) {
    }

    public void setErrorHandler(ErrorHandler handler) {
    }

    public void setFeature(String name, boolean value) {
    }

    public void setProperty(String name, Object value) {
    }

}
