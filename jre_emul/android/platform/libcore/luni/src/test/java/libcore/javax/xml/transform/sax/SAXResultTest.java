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
import static org.junit.Assert.assertNull;

import javax.xml.transform.sax.SAXResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.xml.sax.ContentHandler;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

@RunWith(JUnit4.class)
public class SAXResultTest {

    private SAXResult result;

    @Before
    public void setUp() {
        result = new SAXResult();
    }

    @Test
    public void constructor() {
        result = new SAXResult();
        assertNull(result.getHandler());
        assertNull(result.getLexicalHandler());
        assertNull(result.getSystemId());
    }

    @Test
    public void constructor_withContentHandler() {
        ContentHandler handler = new DefaultHandler();
        result = new SAXResult(handler);
        assertEquals(handler, result.getHandler());
        assertNull(result.getLexicalHandler());
        assertNull(result.getSystemId());
    }

    @Test
    public void getSetHandler() {
        assertNull(result.getHandler());

        ContentHandler handler = new DefaultHandler();
        result.setHandler(handler);
        assertEquals(handler, result.getHandler());

        result.setHandler(null);
        assertNull(result.getHandler());
    }

    @Test
    public void getSetLexicalHandler() {
        assertNull(result.getLexicalHandler());

        LexicalHandler handler = new DefaultHandler2();
        result.setLexicalHandler(handler);
        assertEquals(handler, result.getLexicalHandler());

        result.setLexicalHandler(null);
        assertNull(result.getLexicalHandler());
    }

    @Test
    public void getSetSystemId() {
        assertNull(result.getSystemId());

        String systemId = "systemId";
        result.setSystemId(systemId);
        assertEquals(systemId, result.getSystemId());

        result.setSystemId(null);
        assertNull(result.getSystemId());
    }

}
