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

package libcore.javax.xml.transform.sax;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

@RunWith(JUnit4.class)
public class SAXSourceTest {

    private SAXSource source;

    @Before
    public void setUp() {
        source = new SAXSource();
    }

    @Test
    public void constructor() {
        source = new SAXSource();
        assertNull(source.getInputSource());
        assertNull(source.getXMLReader());
    }

    @Test
    public void constructor_withXMLReaderAndInputSource() {
        XMLReader reader = new XMLReaderImpl();
        InputSource is = new InputSource();
        source = new SAXSource(reader, is);
        assertEquals(reader, source.getXMLReader());
        assertEquals(is, source.getInputSource());
    }

    @Test
    public void setInputSource() {
        InputSource is = new InputSource();
        source.setInputSource(is);
        assertEquals(is, source.getInputSource());

        source.setInputSource(null);
        assertNull(source.getInputSource());
    }

    @Test
    public void setXMLReader() {
        XMLReader reader = new XMLReaderImpl();
        source.setXMLReader(reader);
        assertEquals(reader, source.getXMLReader());

        source.setXMLReader(null);
        assertNull(source.getXMLReader());
    }

    @Test
    public void sourceToInputSource() {
        XMLReader reader = new XMLReaderImpl();
        InputSource is = new InputSource();
        source = new SAXSource(reader, is);
        assertEquals(is, SAXSource.sourceToInputSource(source));

        StreamSource ss = new StreamSource(new StringReader("<tag></tag>"));
        assertNotNull(SAXSource.sourceToInputSource(ss));

        assertNull(SAXSource.sourceToInputSource(new UnknownSource()));
        assertNull(SAXSource.sourceToInputSource(null));
    }

    private static final class UnknownSource implements Source {
        private String systemId;

        public void setSystemId(String systemId) {
            this.systemId = systemId;
        }

        public String getSystemId() {
            return systemId;
        }
    }

    private static final class XMLReaderImpl implements XMLReader {

        @Override
        public boolean getFeature(String name)
                throws SAXNotRecognizedException, SAXNotSupportedException { return false; }

        @Override
        public void setFeature(String name, boolean value)
                throws SAXNotRecognizedException, SAXNotSupportedException {}

        @Override
        public Object getProperty(String name)
                throws SAXNotRecognizedException, SAXNotSupportedException { return null; }

        @Override
        public void setProperty(String name, Object value)
                throws SAXNotRecognizedException, SAXNotSupportedException {}

        @Override
        public void setEntityResolver(EntityResolver resolver) {}

        @Override
        public EntityResolver getEntityResolver() { return null; }

        @Override
        public void setDTDHandler(DTDHandler handler) {}

        @Override
        public DTDHandler getDTDHandler() { return null; }

        @Override
        public void setContentHandler(ContentHandler handler) {}

        @Override
        public ContentHandler getContentHandler() { return null; }

        @Override
        public void setErrorHandler(ErrorHandler handler) {}

        @Override
        public ErrorHandler getErrorHandler() { return null; }

        @Override
        public void parse(InputSource input) throws IOException, SAXException {}

        @Override
        public void parse(String systemId) throws IOException, SAXException {}
    }
}
