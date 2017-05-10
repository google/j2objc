/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.java.util.logging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.MemoryHandler;
import junit.framework.TestCase;

public class OldMemoryHandlerTest extends TestCase {

    final static LogManager manager = LogManager.getLogManager();
    final static Properties props = new Properties();
    final static String baseClassName = OldMemoryHandlerTest.class.getName();
    MemoryHandler handler;

    @Override protected void setUp() throws Exception {
        super.setUp();
        manager.reset();
        initProps();
        manager.readConfiguration(propertiesToInputStream(props));
        handler = new MemoryHandler();
    }

    @Override protected void tearDown() throws Exception {
        super.tearDown();
        manager.readConfiguration();
        props.clear();
    }

    public static InputStream propertiesToInputStream(Properties p) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        p.store(bos, "");
        return new ByteArrayInputStream(bos.toByteArray());
    }

    private void initProps() {
        props.put("java.util.logging.MemoryHandler.level", "FINE");
        props.put("java.util.logging.MemoryHandler.filter", baseClassName + "$MockFilter");
        props.put("java.util.logging.MemoryHandler.size", "2");
        props.put("java.util.logging.MemoryHandler.push", "WARNING");
        props.put("java.util.logging.MemoryHandler.target", baseClassName + "$MockHandler");
        props.put("java.util.logging.MemoryHandler.formatter", baseClassName + "$MockFormatter");
    }

    public void testIsLoggable() {
        assertTrue(handler.isLoggable(new LogRecord(Level.INFO, "1")));
        assertTrue(handler.isLoggable(new LogRecord(Level.WARNING, "2")));
        assertTrue(handler.isLoggable(new LogRecord(Level.SEVERE, "3")));
    }

    public void testMemoryHandler() throws IOException {
        assertNotNull("Filter should not be null", handler.getFilter());
        assertNotNull("Formatter should not be null", handler.getFormatter());
        assertNull("character encoding should be null", handler.getEncoding());
        assertNotNull("ErrorManager should not be null", handler.getErrorManager());
        assertEquals("Level should be FINE", Level.FINE, handler.getLevel());
        assertEquals("Level should be WARNING", Level.WARNING, handler.getPushLevel());
    }

    public static class MockFilter implements Filter {
        public boolean isLoggable(LogRecord record) {
            return !record.getMessage().equals("false");
        }
    }

    public static class MockHandler extends Handler {
        public void close() {}
        public void flush() {}
        public void publish(LogRecord record) {}
    }
}
