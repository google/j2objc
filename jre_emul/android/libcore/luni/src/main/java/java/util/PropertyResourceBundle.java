/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * {@code PropertyResourceBundle} loads resources from an {@code InputStream}. All resources are
 * Strings. The resources must be of the form {@code key=value}, one
 * resource per line (see Properties).
 *
 * @see ResourceBundle
 * @see Properties
 * @since 1.1
 */
public class PropertyResourceBundle extends ResourceBundle {

    Properties resources;

    /**
     * Constructs a new instance of {@code PropertyResourceBundle} and loads the
     * properties file from the specified {@code InputStream}.
     *
     * @param stream
     *            the {@code InputStream}.
     * @throws IOException
     *             if an error occurs during a read operation on the
     *             {@code InputStream}.
     */
    public PropertyResourceBundle(InputStream stream) throws IOException {
        if (stream == null) {
            throw new NullPointerException("stream == null");
        }
        resources = new Properties();
        resources.load(stream);
    }

    /**
     * Constructs a new resource bundle with properties read from {@code reader}.
     *
     * @param reader the {@code Reader}
     * @throws IOException
     * @since 1.6
     */
    public PropertyResourceBundle(Reader reader) throws IOException {
        resources = new Properties();
        resources.load(reader);
    }

    protected Set<String> handleKeySet(){
        return resources.stringPropertyNames();
    }

    @SuppressWarnings("unchecked")
    private Enumeration<String> getLocalKeys() {
        return (Enumeration<String>) resources.propertyNames();
    }

    @Override
    public Enumeration<String> getKeys() {
        if (parent == null) {
            return getLocalKeys();
        }
        return new Enumeration<String>() {
            Enumeration<String> local = getLocalKeys();

            Enumeration<String> pEnum = parent.getKeys();

            String nextElement;

            private boolean findNext() {
                if (nextElement != null) {
                    return true;
                }
                while (pEnum.hasMoreElements()) {
                    String next = pEnum.nextElement();
                    if (!resources.containsKey(next)) {
                        nextElement = next;
                        return true;
                    }
                }
                return false;
            }

            public boolean hasMoreElements() {
                if (local.hasMoreElements()) {
                    return true;
                }
                return findNext();
            }

            public String nextElement() {
                if (local.hasMoreElements()) {
                    return local.nextElement();
                }
                if (findNext()) {
                    String result = nextElement;
                    nextElement = null;
                    return result;
                }
                // Cause an exception
                return pEnum.nextElement();
            }
        };
    }

    @Override
    public Object handleGetObject(String key) {
        return resources.get(key);
    }
}
