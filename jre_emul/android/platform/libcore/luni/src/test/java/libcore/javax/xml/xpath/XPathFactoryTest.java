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

package libcore.javax.xml.xpath;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;

@RunWith(JUnit4.class)
public class XPathFactoryTest {
    public static final String SUPPORTED_URI = "http://a.b.c/d";
    public static final String UNSUPPORTED_URI = "http://a.b.c/e";

    static public class XPathFactoryForTest extends XPathFactory {
        public XPathFactoryForTest() {}

        public boolean getFeature(String name) { return true; }

        public boolean isObjectModelSupported(String objectModel) {
            return objectModel.equals(SUPPORTED_URI);
        }

        public XPath newXPath() { return null; }

        public void setFeature(String name, boolean value) {}

        public void setXPathFunctionResolver(XPathFunctionResolver resolver) {}

        public void setXPathVariableResolver(XPathVariableResolver resolver) {}

        boolean field = false;
    }

    @Test
    public void newInstanceWithUriAndFactoryAndClassLoader() throws Throwable {
        final ClassLoader classLoader = XPathFactoryForTest.class.getClassLoader();
        final String factoryName = XPathFactoryForTest.class.getName();

        // Happy path
        {
            XPathFactory factory = XPathFactory.newInstance(SUPPORTED_URI,
                                                            factoryName,
                                                            classLoader);
            assertNotNull(factory);
        }

        // Bad URI
        try {
            XPathFactory factory = XPathFactory.newInstance(UNSUPPORTED_URI,
                                                            factoryName,
                                                            classLoader);
            fail("Failed for URI " + UNSUPPORTED_URI);
        } catch (XPathFactoryConfigurationException e) {
        }

        // Null URI
        try {
            XPathFactory factory = XPathFactory.newInstance(null, factoryName, classLoader);
            fail("Expected NPE Failed for null URI");
        } catch (NullPointerException e) {
            // Expected
        }

        // Bad factory name
        try {
            final String badFactoryName = factoryName + "Bad";
            XPathFactory factory = XPathFactory.newInstance(SUPPORTED_URI,
                                                            badFactoryName,
                                                            classLoader);
            fail("Failed for " + badFactoryName);
        } catch (XPathFactoryConfigurationException e) {
        }

        // Null ClassLoader
        {
            XPathFactory factory = XPathFactory.newInstance(SUPPORTED_URI, factoryName, null);
            assertNotNull(factory);
        }
    }
}
