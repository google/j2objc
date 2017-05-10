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

import java.io.UnsupportedEncodingException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.XMLFormatter;
import junit.framework.TestCase;

public final class OldXMLFormatterTest extends TestCase {

    XMLFormatter formatter = null;
    MockHandler handler = null;
    LogRecord lr = null;

    @Override protected void setUp() throws Exception {
        super.setUp();
        formatter = new XMLFormatter();
        handler = new MockHandler();
        lr = new LogRecord(Level.SEVERE, "pattern");
    }

    public void testXMLFormatter() throws SecurityException, UnsupportedEncodingException {
        handler.setEncoding("UTF-8");

        String result = formatter.getHead(handler);
        int headPos = result
                .indexOf("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
        int dtdPos = result.indexOf("<!DOCTYPE log SYSTEM \"logger.dtd\">");
        int rootPos = result.indexOf("<log>");
        assertTrue("head string position should be more or equal zero",
                headPos >= 0);
        assertTrue("dtd string position should be more head string position",
                dtdPos > headPos);
        assertTrue("root string position should be more dtd string position",
                rootPos > dtdPos);

        assertTrue("Tail string position should be more zero", formatter
                .getTail(handler).indexOf("/log>") > 0);
    }

    public void testGetTail() {
        assertEquals("Tail string with null handler should be equal expected value",
                "</log>", formatter.getTail(null).trim());
        assertEquals("Tail string should be equal expected value", "</log>",
                formatter.getTail(handler).trim());
        handler.publish(lr);
        assertEquals("Tail string after publish() should be equal expected value",
                "</log>", formatter.getTail(handler).trim());
    }

    public static class MockHandler extends Handler {
        public void close() {}
        public void flush() {}
        public void publish(LogRecord record) {}
    }
}
