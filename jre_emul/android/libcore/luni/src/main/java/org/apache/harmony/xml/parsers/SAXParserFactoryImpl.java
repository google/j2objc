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

import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXNotRecognizedException;

/**
 * Provides a straightforward SAXParserFactory implementation based on
 * Expat. The class is used internally only, thus only notable members
 * that are not already in the abstract superclass are documented.
 */
public class SAXParserFactoryImpl extends SAXParserFactory {

    private static final String NAMESPACES
            = "http://xml.org/sax/features/namespaces";

    private static final String VALIDATION
            = "http://xml.org/sax/features/validation";

    private Map<String, Boolean> features = new HashMap<String, Boolean>();

    @Override
    public boolean getFeature(String name) throws SAXNotRecognizedException {
        if (name == null) {
            throw new NullPointerException("name == null");
        }

        if (!name.startsWith("http://xml.org/sax/features/")) {
            throw new SAXNotRecognizedException(name);
        }

        return Boolean.TRUE.equals(features.get(name));
    }

    @Override
    public boolean isNamespaceAware() {
        try {
            return getFeature(NAMESPACES);
        } catch (SAXNotRecognizedException ex) {
            throw new AssertionError(ex);
        }
    }

    @Override
    public boolean isValidating() {
        try {
            return getFeature(VALIDATION);
        } catch (SAXNotRecognizedException ex) {
            throw new AssertionError(ex);
        }
    }

    @Override
    public SAXParser newSAXParser() throws ParserConfigurationException {
        if (isValidating()) {
            throw new ParserConfigurationException(
                    "No validating SAXParser implementation available");
        }

        try {
            return new SAXParserImpl(features);
        } catch (Exception ex) {
            throw new ParserConfigurationException(ex.toString());
        }
    }

    @Override
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException {
        if (name == null) {
            throw new NullPointerException("name == null");
        }

        if (!name.startsWith("http://xml.org/sax/features/")) {
            throw new SAXNotRecognizedException(name);
        }

        if (value) {
            features.put(name, Boolean.TRUE);
        } else {
            // This is needed to disable features that are enabled by default.
            features.put(name, Boolean.FALSE);
        }
    }

    @Override
    public void setNamespaceAware(boolean value) {
        try {
            setFeature(NAMESPACES, value);
        } catch (SAXNotRecognizedException ex) {
            throw new AssertionError(ex);
        }
    }

    @Override
    public void setValidating(boolean value) {
        try {
            setFeature(VALIDATION, value);
        } catch (SAXNotRecognizedException ex) {
            throw new AssertionError(ex);
        }
    }
}
