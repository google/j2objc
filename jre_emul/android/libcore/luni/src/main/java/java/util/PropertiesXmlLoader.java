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

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Dynamically loaded implementation for Properties.loadFromXML(). Public so that users can add an
 * explicit dependency to force load this class.
 */
public class PropertiesXmlLoader implements Properties.XmlLoader {

    public void load(final Properties p, InputStream in) throws IOException,
            InvalidPropertiesFormatException {
        if (in == null) {
            throw new NullPointerException("in == null");
        }

        try {
            XMLReader reader = XMLReaderFactory.createXMLReader();
            reader.setContentHandler(new DefaultHandler() {
                private String key;

                @Override
                public void startElement(String uri, String localName,
                        String qName, Attributes attributes) throws SAXException {
                    key = null;
                    if (qName.equals("entry")) {
                        key = attributes.getValue("key");
                    }
                }

                @Override
                public void characters(char[] ch, int start, int length)
                        throws SAXException {
                    if (key != null) {
                        String value = new String(ch, start, length);
                        p.put(key, value);
                        key = null;
                    }
                }
            });
            reader.parse(new InputSource(in));
        } catch (SAXException e) {
            throw new InvalidPropertiesFormatException(e);
        }
    }
}
