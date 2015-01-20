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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Provides a straightforward DocumentBuilderFactory implementation based on
 * XMLPull/KXML. The class is used internally only, thus only notable members
 * that are not already in the abstract superclass are documented. Hope that's
 * ok.
 */
public class DocumentBuilderFactoryImpl extends DocumentBuilderFactory {

    private static final String NAMESPACES =
            "http://xml.org/sax/features/namespaces";

    private static final String VALIDATION =
            "http://xml.org/sax/features/validation";

    @Override
    public Object getAttribute(String name) throws IllegalArgumentException {
        throw new IllegalArgumentException(name);
    }

    @Override
    public boolean getFeature(String name) throws ParserConfigurationException {
        if (name == null) {
            throw new NullPointerException("name == null");
        }

        if (NAMESPACES.equals(name)) {
            return isNamespaceAware();
        } else if (VALIDATION.equals(name)) {
            return isValidating();
        } else {
            throw new ParserConfigurationException(name);
        }
    }

    @Override
    public DocumentBuilder newDocumentBuilder()
            throws ParserConfigurationException {
        if (isValidating()) {
            throw new ParserConfigurationException(
                    "No validating DocumentBuilder implementation available");
        }

        /**
         * TODO If Android is going to support a different DocumentBuilder
         * implementations, this should be wired here. If we wanted to
         * allow different implementations, these could be distinguished by
         * a special feature (like http://www.org.apache.harmony.com/xml/expat)
         * or by throwing the full SPI monty at it.
         */
        DocumentBuilderImpl builder = new DocumentBuilderImpl();
        builder.setCoalescing(isCoalescing());
        builder.setIgnoreComments(isIgnoringComments());
        builder.setIgnoreElementContentWhitespace(isIgnoringElementContentWhitespace());
        builder.setNamespaceAware(isNamespaceAware());

        // TODO What about expandEntityReferences?

        return builder;
    }

    @Override
    public void setAttribute(String name, Object value)
            throws IllegalArgumentException {
        throw new IllegalArgumentException(name);
    }

    @Override
    public void setFeature(String name, boolean value)
            throws ParserConfigurationException {
        if (name == null) {
            throw new NullPointerException("name == null");
        }

        if (NAMESPACES.equals(name)) {
            setNamespaceAware(value);
        } else if (VALIDATION.equals(name)) {
            setValidating(value);
        } else {
            throw new ParserConfigurationException(name);
        }
    }

}
