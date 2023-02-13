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

package libcore.javax.xml.transform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TransformerExceptionTest {

    @Test
    public void constructorWithStringAndThrowable() {
        Throwable t = new Throwable();
        TransformerException e = new TransformerException(t);
        assertEquals("java.lang.Throwable", e.getMessage());
        assertEquals(t, e.getCause());
    }

    @Test
    public void constructorWithStringAndSourceLocator() {
        SourceLocator locator = new SourceLocatorImpl();
        TransformerException e = new TransformerException("message", locator);
        assertEquals("message", e.getMessage());
        assertNull(e.getCause());
        assertEquals(locator, e.getLocator());
    }

    @Test
    public void constructorWithStringSourceLocatorAndThrowable() {
        SourceLocator locator = new SourceLocatorImpl();
        Throwable t = new Throwable();
        TransformerException e = new TransformerException("message", locator, t);
        assertEquals("message", e.getMessage());
        assertEquals(t, e.getCause());
        assertEquals(locator, e.getLocator());
    }

    @Test
    public void getException() {
        TransformerException e = new TransformerException("message");
        Throwable t = e.getException();
        assertEquals("message", e.getMessage());
        assertEquals(t, e.getCause());
    }

    @Test
    public void getMessageAndLocation() {
        SourceLocator locator = new SourceLocatorImpl();
        TransformerException e = new TransformerException("message", locator);
        assertEquals("message", e.getMessageAndLocation());
    }

}