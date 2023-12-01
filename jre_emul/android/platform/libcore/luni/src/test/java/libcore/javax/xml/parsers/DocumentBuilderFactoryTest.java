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
package libcore.javax.xml.parsers;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import javax.xml.validation.ValidatorHandler;
import junit.framework.TestCase;

public class DocumentBuilderFactoryTest extends TestCase {

    public void testGetSchema() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {
            factory.getSchema();
            fail("Unexpectedly didn't throw UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {}
    }

    public void testSetSchema() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {
            factory.setSchema(new Schema() {
                @Override
                public Validator newValidator() { return null; }

                @Override
                public ValidatorHandler newValidatorHandler() { return null; }
            });
            fail("Unexpectedly didn't throw UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {}
    }

    public void testNewInstance_StringClassLoader() {
        try {
            DocumentBuilderFactory.newInstance(null, null);
        } catch (FactoryConfigurationError expected) {}
    }
}
