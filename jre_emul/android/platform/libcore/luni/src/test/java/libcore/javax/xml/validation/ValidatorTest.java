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

import java.io.IOException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Validator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;

@RunWith(JUnit4.class)
public class ValidatorTest {

    private Validator validator;

    @Before
    public void setUp() {
        validator = new ValidatorImpl();
    }

    @Test
    public void constructor() {
        validator = new ValidatorImpl();
        assertNotNull(validator);
    }

    @Test
    public void getFeature() {
        assertThrows(NullPointerException.class, () -> validator.getFeature(null));
        assertThrows(SAXNotRecognizedException.class, () -> validator.getFeature("hello"));
        assertThrows(SAXNotRecognizedException.class, () -> validator.getFeature(""));
    }

    @Test
    public void getProperty() {
        assertThrows(NullPointerException.class, () -> validator.getProperty(null));
        assertThrows(SAXNotRecognizedException.class, () -> validator.getProperty("hello"));
        assertThrows(SAXNotRecognizedException.class, () -> validator.getProperty(""));
    }

    @Test
    public void setFeature() {
        assertThrows(NullPointerException.class,
                () -> validator.setFeature(null, false));
        assertThrows(NullPointerException.class,
                () -> validator.setFeature(null, true));

        String[] features = {"", "hello", "feature"};
        boolean[] trueAndFalse = {true, false};
        for (String feature : features) {
            for (boolean value : trueAndFalse) {
                assertThrows(SAXNotRecognizedException.class,
                        () -> validator.setFeature(feature, value));
            }
        }
    }

    @Test
    public void setProperty() {
        assertThrows(NullPointerException.class,
                () -> validator.setProperty(null, false));
        assertThrows(NullPointerException.class,
                () -> validator.setProperty(null, true));

        String[] properties = {"", "hello", "property"};
        boolean[] trueAndFalse = {true, false};
        for (String property : properties) {
            for (boolean value : trueAndFalse) {
                assertThrows(SAXNotRecognizedException.class,
                        () -> validator.setProperty(property, value));
            }
        }
    }

    @Test
    public void validate() throws IOException, SAXException {
        Source source = new SAXSource();
        validator.validate(source);
    }

    private static final class ValidatorImpl extends Validator {

        @Override
        public void reset() {
        }

        @Override
        public void validate(Source source, Result result) throws SAXException, IOException {
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
    }
}
