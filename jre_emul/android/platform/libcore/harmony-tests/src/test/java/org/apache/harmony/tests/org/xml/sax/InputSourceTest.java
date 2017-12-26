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

package org.apache.harmony.tests.org.xml.sax;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import junit.framework.TestCase;

import org.xml.sax.InputSource;

public class InputSourceTest extends TestCase {

    public void testInputSource() {
        InputSource i = new InputSource();

        assertNull(i.getByteStream());
        assertNull(i.getCharacterStream());
        assertNull(i.getEncoding());
        assertNull(i.getPublicId());
        assertNull(i.getSystemId());
    }

    public void testInputSourceString() {
        InputSource i = new InputSource("Foo");

        assertNull(i.getByteStream());
        assertNull(i.getCharacterStream());
        assertNull(i.getEncoding());
        assertNull(i.getPublicId());
        assertEquals("Foo", i.getSystemId());
    }

    public void testInputSourceInputStream() {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);

        // Ordinary case
        InputSource i = new InputSource(bais);

        assertEquals(bais, i.getByteStream());
        assertNull(i.getCharacterStream());
        assertNull(i.getEncoding());
        assertNull(i.getPublicId());
        assertNull(i.getSystemId());

        // No input stream
        i = new InputSource((InputStream)null);

        assertNull(i.getByteStream());
        assertNull(i.getCharacterStream());
        assertNull(i.getEncoding());
        assertNull(i.getPublicId());
        assertNull(i.getSystemId());
    }

    public void testInputSourceReader() {
        StringReader sr = new StringReader("Hello, world.");

        // Ordinary case
        InputSource i = new InputSource(sr);

        assertNull(i.getByteStream());
        assertEquals(sr, i.getCharacterStream());
        assertNull(i.getEncoding());
        assertNull(i.getPublicId());
        assertNull(i.getSystemId());

        // No reader
        i = new InputSource((Reader)null);

        assertNull(i.getByteStream());
        assertNull(i.getCharacterStream());
        assertNull(i.getEncoding());
        assertNull(i.getPublicId());
        assertNull(i.getSystemId());
    }

    public void testSetPublicIdGetPublicId() {
        InputSource i = new InputSource();

        i.setPublicId("Foo");
        assertEquals("Foo", i.getPublicId());

        i.setPublicId(null);
        assertNull(i.getPublicId());
    }

    public void testSetSystemIdGetSystemId() {
        InputSource i = new InputSource();

        i.setSystemId("Foo");
        assertEquals("Foo", i.getSystemId());

        i.setSystemId(null);
        assertNull(i.getSystemId());
    }

    public void testSetByteStreamGetByteStream() {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);

        InputSource i = new InputSource();

        // Ordinary case
        i.setByteStream(bais);

        assertEquals(bais, i.getByteStream());

        // No input stream
        i.setByteStream(null);

        assertNull(i.getByteStream());
    }

    public void testSetEncodingGetEncoding() {
        InputSource i = new InputSource();

        // Ordinary case
        i.setEncoding("Klingon");

        assertEquals("Klingon", i.getEncoding());

        // No encoding
        i.setEncoding(null);

        assertNull(i.getEncoding());
    }

    public void testSetCharacterStreamGetCharacterStream() {
        StringReader sr = new StringReader("Hello, world.");

        InputSource i = new InputSource();

        // Ordinary case
        i.setCharacterStream(sr);

        assertNull(i.getByteStream());
        assertEquals(sr, i.getCharacterStream());
        assertNull(i.getEncoding());
        assertNull(i.getPublicId());
        assertNull(i.getSystemId());

        // No reader
        i.setCharacterStream(null);

        assertNull(i.getByteStream());
        assertNull(i.getCharacterStream());
        assertNull(i.getEncoding());
        assertNull(i.getPublicId());
        assertNull(i.getSystemId());
    }

}
