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

package org.apache.harmony.xml.parsers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.SAXParser;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderAdapter;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.sax2.Driver;

/**
 * A SAX parser based on Expat.
 */
final class SAXParserImpl extends SAXParser {

    private Map<String, Boolean> initialFeatures;
    private XMLReader reader;
    private Parser parser;

    SAXParserImpl(Map<String, Boolean> initialFeatures)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        this.initialFeatures = initialFeatures.isEmpty()
                ? Collections.<String, Boolean>emptyMap()
                : new HashMap<String, Boolean>(initialFeatures);
        resetInternal();
    }

    private void resetInternal()
            throws SAXNotSupportedException, SAXNotRecognizedException {
	try {
            reader = new Driver();
            for (Map.Entry<String,Boolean> entry : initialFeatures.entrySet()) {
                reader.setFeature(entry.getKey(), entry.getValue());
            }
	} catch (XmlPullParserException e) {
	    throw new SAXNotRecognizedException(e.toString());
	}
    }

    @Override public void reset() {
        /*
         * The exceptions are impossible. If any features are unrecognized or
         * unsupported, construction of this instance would have failed.
         */
        try {
            resetInternal();
        } catch (SAXNotRecognizedException e) {
            throw new AssertionError();
        } catch (SAXNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public Parser getParser() {
        if (parser == null) {
            parser = new XMLReaderAdapter(reader);
        }

        return parser;
    }

    @Override
    public Object getProperty(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        return reader.getProperty(name);
    }

    @Override
    public XMLReader getXMLReader() {
        return reader;
    }

    @Override
    public boolean isNamespaceAware() {
        try {
            return reader.getFeature("http://xml.org/sax/features/namespaces");
        } catch (SAXException ex) {
            return false;
        }
    }

    @Override
    public boolean isValidating() {
        return false;
    }

    @Override
    public void setProperty(String name, Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        reader.setProperty(name, value);
    }
}
