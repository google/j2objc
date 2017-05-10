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

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;
import junit.framework.TestCase;

public class OldFormatterTest extends TestCase {
    static String MSG = "msg, pls. ignore it";

    Formatter f = new MockFormatter();
    LogRecord r = new LogRecord(Level.FINE, MSG);
    Handler h;

    @Override protected void setUp() throws Exception {
        super.setUp();
        h = new StreamHandler();
    }

    public void testFormatter() {
        assertEquals("head string is not empty", "", f.getHead(null));
        assertEquals("tail string is not empty", "", f.getTail(null));

    }

    public void testGetHead() {
        assertEquals("head string is not empty", "", f.getHead(null));
        assertEquals("head string is not empty", "", f.getHead(h));
        h.publish(r);
        assertEquals("head string is not empty", "", f.getHead(h));
    }

    public void testGetTail() {
        assertEquals("tail string is not empty", "", f.getTail(null));
        assertEquals("tail string is not empty", "", f.getTail(h));
        h.publish(r);
        assertEquals("tail string is not empty", "", f.getTail(h));
    }

    public void testFormatMessage() {
        // The RI fails in this test because it uses a MessageFormat to format
        // the message even though it doesn't contain "{0". The spec says that
        // this would indicate that a MessageFormat should be used and else no
        // formatting should be done.
        String pattern = "pattern without 0 {1, number}";
        r.setMessage(pattern);
        assertEquals(pattern, f.formatMessage(r));
    }

    public static class MockFormatter extends Formatter {
        public String format(LogRecord arg0) {
            return "format";
        }
    }
}
