/*
 * Copyright (C) 2022 The Android Open Source Project
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

package libcore.javax.xml.transform.stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.transform.stream.StreamResult;

@RunWith(JUnit4.class)
public class StreamResultTest {

    @Test
    public void constructor() {
        StreamResult sr = new StreamResult();
        assertNull(sr.getOutputStream());
        assertNull(sr.getSystemId());
        assertNull(sr.getWriter());
    }

    @Test
    public void constructorWithFile() throws IOException {
        final String PREFIX = "StreamResultTest52";
        File file = File.createTempFile(PREFIX, null);
        StreamResult sr = new StreamResult(file);
        assertNull(sr.getOutputStream());
        assertTrue(sr.getSystemId().contains(PREFIX));
        assertNull(sr.getWriter());
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void constructorWithOutputStream() {
        ByteArrayOutputStream os = new ByteArrayOutputStream(16);
        StreamResult sr = new StreamResult(os);
        assertEquals(os, sr.getOutputStream());
        assertNull(sr.getSystemId());
        assertNull(sr.getWriter());
    }

    @Test
    public void constructorWithSystemId() {
        final String ID = "System74";
        StreamResult sr = new StreamResult(ID);
        assertNull(sr.getOutputStream());
        assertEquals(ID, sr.getSystemId());
        assertNull(sr.getWriter());
    }

    @Test
    public void constructorWithWriter() {
        StringWriter sw = new StringWriter();
        StreamResult sr = new StreamResult(sw);
        assertNull(sr.getOutputStream());
        assertNull(sr.getSystemId());
        assertEquals(sw, sr.getWriter());
    }

    @Test
    public void setOutputStream() {
        StreamResult sr = new StreamResult();
        ByteArrayOutputStream os = new ByteArrayOutputStream(16);
        sr.setOutputStream(os);
        assertEquals(os, sr.getOutputStream());
    }

    @Test
    public void setSystemIdWithFile() throws IOException {
        final String PREFIX = "StreamResultTest100";
        StreamResult sr = new StreamResult();
        File file = File.createTempFile(PREFIX, null);
        sr.setSystemId(file);
        assertTrue(sr.getSystemId().contains(PREFIX));
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void setSystemIdWithString() {
        final String ID = "System112";
        StreamResult sr = new StreamResult();
        sr.setSystemId(ID);
        assertEquals(ID, sr.getSystemId());
    }

    @Test
    public void setWriter() {
        StreamResult sr = new StreamResult();
        StringWriter sw = new StringWriter();
        sr.setWriter(sw);
        assertEquals(sw, sr.getWriter());
    }
}
