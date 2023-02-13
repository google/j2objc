/*
 * Copyright (C) 2021 The Android Open Source Project
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

package libcore.javax.xml.validation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import javax.xml.validation.TypeInfoProvider;
import javax.xml.validation.ValidatorHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;

@RunWith(JUnit4.class)
public class ValidatorHandlerTest {

    private ValidatorHandler handler;

    @Before
    public void setUp() {
        handler = new ValidatorHandlerImpl();
    }

    @Test
    public void constructor() {
        handler = new ValidatorHandlerImpl();
        assertNotNull(handler);
    }

    @Test
    public void getFeature() {
        assertThrows(NullPointerException.class, () -> handler.getFeature(null));
        assertThrows(SAXNotRecognizedException.class, () -> handler.getFeature("hello"));
        assertThrows(SAXNotRecognizedException.class, () -> handler.getFeature(""));
    }

    @Test
    public void getProperty() {
        assertThrows(NullPointerException.class, () -> handler.getProperty(null));
        assertThrows(SAXNotRecognizedException.class, () -> handler.getProperty("hello"));
        assertThrows(SAXNotRecognizedException.class, () -> handler.getProperty(""));
    }

    @Test
    public void setFeature() {
        assertThrows(NullPointerException.class,
                () -> handler.setFeature(null, false));
        assertThrows(NullPointerException.class,
                () -> handler.setFeature(null, true));

        String[] features = {"", "hello", "feature"};
        boolean[] trueAndFalse = {true, false};
        for (String feature : features) {
            for (boolean value : trueAndFalse) {
                assertThrows(SAXNotRecognizedException.class,
                        () -> handler.setFeature(feature, value));
            }
        }
    }

    @Test
    public void setProperty() {
        assertThrows(NullPointerException.class,
                () -> handler.setProperty(null, false));
        assertThrows(NullPointerException.class,
                () -> handler.setProperty(null, true));

        String[] properties = {"", "hello", "property"};
        boolean[] trueAndFalse = {true, false};
        for (String property : properties) {
            for (boolean value : trueAndFalse) {
                assertThrows(SAXNotRecognizedException.class,
                        () -> handler.setProperty(property, value));
            }
        }
    }

    private static final class ValidatorHandlerImpl extends ValidatorHandler {

        @Override
        public ContentHandler getContentHandler() {
            return null;
        }

        @Override
        public void setContentHandler(ContentHandler receiver) {
        }

        @Override
        public ErrorHandler getErrorHandler() {
            return null;
        }

        @Override
        public void setErrorHandler(ErrorHandler errorHandler) {
        }

        @Override
        public LSResourceResolver getResourceResolver() {
            return null;
        }

        @Override
        public void setResourceResolver(LSResourceResolver resourceResolver) {
        }

        @Override
        public TypeInfoProvider getTypeInfoProvider() {
            return null;
        }

        @Override
        public void setDocumentLocator(Locator locator) {
        }

        @Override
        public void startDocument() throws SAXException {
        }

        @Override
        public void endDocument() throws SAXException {
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException {
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts)
                throws SAXException {
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        }

        @Override
        public void processingInstruction(String target, String data) throws SAXException {
        }

        @Override
        public void skippedEntity(String name) throws SAXException {
        }
    }
}
