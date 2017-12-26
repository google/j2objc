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

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * A helper class that extends XMLFilterImpl, provides dummy feature/property
 * management, and logs some method calls.
 */
public class MockFilter extends XMLFilterImpl {

    private MethodLogger logger;

    private Set<String> features = new HashSet<String>();

    private Map<String, Object> properties = new HashMap<String, Object>();

    public MockFilter(MethodLogger logger) {
        super();
        this.logger = logger;
    }

    @Override
    public boolean getFeature(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        return features.contains(name);
    }

    @Override
    public Object getProperty(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        return properties.get(name);
    }

    @Override
    public void setFeature(String name, boolean value) {
        if (value) {
            features.add(name);
        } else {
            features.remove(name);
        }
    }

    @Override
    public void setProperty(String name, Object value) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        if (value == null) {
            properties.remove(name);
        } else {
            properties.put(name, value);
        }
    }

    @Override
    public void parse(InputSource input) throws SAXException, IOException {
        logger.add("parse", input);
    }

    @Override
    public void parse(String systemId) throws SAXException, IOException {
        logger.add("parse", systemId);
    }

}
