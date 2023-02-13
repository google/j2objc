/*
 * Copyright (C) 2009 The Android Open Source Project
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

import static tests.support.Support_Xml.attrOf;
import static tests.support.Support_Xml.domOf;
import static tests.support.Support_Xml.firstChildTextOf;
import static tests.support.Support_Xml.firstElementOf;

import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;

public class DocumentBuilderTest extends junit.framework.TestCase {
    // http://code.google.com/p/android/issues/detail?id=2607
    public void test_characterReferences() throws Exception {
        assertEquals("aAb", firstChildTextOf(domOf("<p>a&#65;b</p>")));
        assertEquals("aAb", firstChildTextOf(domOf("<p>a&#x41;b</p>")));
        assertEquals("a\u00fcb", firstChildTextOf(domOf("<p>a&#252;b</p>")));
        assertEquals("a\u00fcb", firstChildTextOf(domOf("<p>a&#xfc;b</p>")));
    }

    // http://code.google.com/p/android/issues/detail?id=2607
    public void test_predefinedEntities() throws Exception {
        assertEquals("a<b", firstChildTextOf(domOf("<p>a&lt;b</p>")));
        assertEquals("a>b", firstChildTextOf(domOf("<p>a&gt;b</p>")));
        assertEquals("a&b", firstChildTextOf(domOf("<p>a&amp;b</p>")));
        assertEquals("a'b", firstChildTextOf(domOf("<p>a&apos;b</p>")));
        assertEquals("a\"b", firstChildTextOf(domOf("<p>a&quot;b</p>")));
    }

    // http://code.google.com/p/android/issues/detail?id=2487
    public void test_cdata_attributes() throws Exception {
        assertEquals("hello & world", attrOf(firstElementOf(domOf("<?xml version=\"1.0\"?><root attr=\"hello &amp; world\" />"))));
        try {
            domOf("<?xml version=\"1.0\"?><root attr=\"hello <![CDATA[ some-cdata ]]> world\" />");
            fail("SAXParseException not thrown");
        } catch (org.xml.sax.SAXParseException ex) {
            // Expected.
        }
        assertEquals("hello <![CDATA[ some-cdata ]]> world", attrOf(firstElementOf(domOf("<?xml version=\"1.0\"?><root attr=\"hello &lt;![CDATA[ some-cdata ]]&gt; world\" />"))));
        assertEquals("hello <![CDATA[ some-cdata ]]> world", attrOf(firstElementOf(domOf("<?xml version=\"1.0\"?><root attr=\"hello &lt;![CDATA[ some-cdata ]]> world\" />"))));
    }

    // http://code.google.com/p/android/issues/detail?id=2487
    public void test_cdata_body() throws Exception {
        assertEquals("hello & world", firstChildTextOf(domOf("<?xml version=\"1.0\"?><root>hello &amp; world</root>")));
        assertEquals("hello  some-cdata  world", firstChildTextOf(domOf("<?xml version=\"1.0\"?><root>hello <![CDATA[ some-cdata ]]> world</root>")));
        assertEquals("hello <![CDATA[ some-cdata ]]> world", firstChildTextOf(domOf("<?xml version=\"1.0\"?><root>hello &lt;![CDATA[ some-cdata ]]&gt; world</root>")));
        try {
            domOf("<?xml version=\"1.0\"?><root>hello &lt;![CDATA[ some-cdata ]]> world</root>");
            fail("SAXParseException not thrown");
        } catch (org.xml.sax.SAXParseException ex) {
            // Expected.
        }
    }

    public void testGetSchema() {
        DocumentBuilder db = new DocumentBuilder() {
            @Override
            public Document parse(InputSource is) { return null; }

            @Override
            public boolean isNamespaceAware() { return false; }

            @Override
            public boolean isValidating() { return false; }

            @Override
            public void setEntityResolver(EntityResolver er) {}

            @Override
            public void setErrorHandler(ErrorHandler eh) {}

            @Override
            public Document newDocument() { return null; }

            @Override
            public DOMImplementation getDOMImplementation() { return null; }
        };

        try {
            db.getSchema();
            fail("Unexpectedly didn't throw UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {}
    }

}
