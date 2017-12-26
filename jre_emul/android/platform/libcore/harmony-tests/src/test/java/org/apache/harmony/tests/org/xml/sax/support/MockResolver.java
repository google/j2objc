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
import java.util.Map;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A helper class for resolving entities.
 */
public class MockResolver implements EntityResolver {

    private Map<String, InputSource> entities = new HashMap<String, InputSource>();

    public void addEntity(String publicId, String systemId, InputSource source) {
        entities.put("[" + publicId + ":" + systemId + "]", source);
    }

    public void removeEntity(String publicId, String systemId) {
        entities.remove("[" + publicId + ":" + systemId + "]");
    }

    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException {
        return entities.get("[" + publicId + ":" + systemId + "]");
    }

}
