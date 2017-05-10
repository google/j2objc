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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import junit.framework.TestCase;

public class OldFileHandlerTest extends TestCase {

    static LogManager manager = LogManager.getLogManager();
    final static Properties props = new Properties();
    final static String className = OldFileHandlerTest.class.getName();
    final static String SEP = File.separator;
    String HOMEPATH;
    String TEMPPATH;
    FileHandler handler;
    LogRecord r;

    protected void setUp() throws Exception {
        super.setUp();
        manager.reset();

        //initProp
        props.clear();
        props.put("java.util.logging.FileHandler.level", "FINE");
        props.put("java.util.logging.FileHandler.filter", className
                + "$MockFilter");
        props.put("java.util.logging.FileHandler.formatter", className
                + "$MockFormatter");
        props.put("java.util.logging.FileHandler.encoding", "iso-8859-1");
        // limit to only two message
        props.put("java.util.logging.FileHandler.limit", "1000");
        // rotation count is 2
        props.put("java.util.logging.FileHandler.count", "2");
        // using append mode
        props.put("java.util.logging.FileHandler.append", "true");
        props.put("java.util.logging.FileHandler.pattern",
                        "%t/log/java%u.test");

        HOMEPATH = System.getProperty("user.home");
        TEMPPATH = System.getProperty("java.io.tmpdir");

        File file = new File(TEMPPATH + SEP + "log");
        file.mkdir();
        manager.readConfiguration(propertiesToInputStream(props));
        handler = new FileHandler();
        r = new LogRecord(Level.CONFIG, "msg");
    }

    protected void tearDown() throws Exception {
        if (null != handler) {
            handler.close();
        }
        reset(TEMPPATH + SEP + "log", "");
        super.tearDown();
    }

    public static InputStream propertiesToInputStream(Properties p) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        p.store(bos, "");
        return new ByteArrayInputStream(bos.toByteArray());
    }

    public void testFileHandler() throws Exception {
        assertEquals("character encoding is non equal to actual value",
                "iso-8859-1", handler.getEncoding());
        assertNotNull("Filter is null", handler.getFilter());
        assertNotNull("Formatter is null", handler.getFormatter());
        assertEquals("is non equal to actual value", Level.FINE, handler
                .getLevel());
        assertNotNull("ErrorManager is null", handler.getErrorManager());
        handler.publish(r);
        handler.close();
        // output 3 times, and all records left
        // append mode is true
        for (int i = 0; i < 3; i++) {
            handler = new FileHandler();
            handler.publish(r);
            handler.close();
        }
        assertFileContent(TEMPPATH + SEP + "log", "java0.test.0",
                new LogRecord[] { r, null, r, null, r, null, r },
                new MockFormatter());
    }

    public void testFileHandler_1params() throws Exception {

        handler = new FileHandler("%t/log/string");
        assertEquals("character encoding is non equal to actual value",
                "iso-8859-1", handler.getEncoding());
        assertNotNull("Filter is null", handler.getFilter());
        assertNotNull("Formatter is null", handler.getFormatter());
        assertEquals("is non equal to actual value", Level.FINE, handler
                .getLevel());
        assertNotNull("ErrorManager is null", handler.getErrorManager());
        handler.publish(r);
        handler.close();

        // output 3 times, and all records left
        // append mode is true
        for (int i = 0; i < 3; i++) {
            handler = new FileHandler("%t/log/string");
            handler.publish(r);
            handler.close();
        }
        assertFileContent(TEMPPATH + SEP + "log", "/string", new LogRecord[] {
                r, null, r, null, r, null, r }, new MockFormatter());

        // test if unique ids not specified, it will append at the end
        // no generation number is used
        FileHandler h = new FileHandler("%t/log/string");
        FileHandler h2 = new FileHandler("%t/log/string");
        FileHandler h3 = new FileHandler("%t/log/string");
        FileHandler h4 = new FileHandler("%t/log/string");
        h.publish(r);
        h2.publish(r);
        h3.publish(r);
        h4.publish(r);
        h.close();
        h2.close();
        h3.close();
        h4.close();
        assertFileContent(TEMPPATH + SEP + "log", "string", h.getFormatter());
        assertFileContent(TEMPPATH + SEP + "log", "string.1", h.getFormatter());
        assertFileContent(TEMPPATH + SEP + "log", "string.2", h.getFormatter());
        assertFileContent(TEMPPATH + SEP + "log", "string.3", h.getFormatter());

        // default is append mode
        FileHandler h6 = new FileHandler("%t/log/string%u.log");
        h6.publish(r);
        h6.close();
        FileHandler h7 = new FileHandler("%t/log/string%u.log");
        h7.publish(r);
        h7.close();
        try {
            assertFileContent(TEMPPATH + SEP + "log", "string0.log", h
                    .getFormatter());
            fail("should assertion failed");
        } catch (Error e) {
        }
        File file = new File(TEMPPATH + SEP + "log");
        assertTrue("length list of file is incorrect", file.list().length <= 2);

        // test unique ids
        FileHandler h8 = new FileHandler("%t/log/%ustring%u.log");
        h8.publish(r);
        FileHandler h9 = new FileHandler("%t/log/%ustring%u.log");
        h9.publish(r);
        h9.close();
        h8.close();
        assertFileContent(TEMPPATH + SEP + "log", "0string0.log", h
                .getFormatter());
        assertFileContent(TEMPPATH + SEP + "log", "1string1.log", h
                .getFormatter());
        file = new File(TEMPPATH + SEP + "log");
        assertTrue("length list of file is incorrect", file.list().length <= 2);

        try {
            new FileHandler("");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }
    }

    public void testFileHandler_2params() throws Exception {
        boolean append = false;
        do {
            append = !append;
            handler = new FileHandler("%t/log/string", append);
            assertEquals("character encoding is non equal to actual value",
                    "iso-8859-1", handler.getEncoding());
            assertNotNull("Filter is null", handler.getFilter());
            assertNotNull("Formatter is null", handler.getFormatter());
            assertEquals("is non equal to actual value", Level.FINE, handler
                    .getLevel());
            assertNotNull("ErrorManager is null", handler.getErrorManager());
            handler.publish(r);
            handler.close();
            // output 3 times, and all records left
            // append mode is true
            for (int i = 0; i < 3; i++) {
                handler = new FileHandler("%t/log/string", append);
                handler.publish(r);
                handler.close();
            }
            if (append) {
                assertFileContent(TEMPPATH + SEP + "log", "/string",
                        new LogRecord[] { r, null, r, null, r, null, r },
                        new MockFormatter());
            } else {
                assertFileContent(TEMPPATH + SEP + "log", "/string",
                        new LogRecord[] { r }, new MockFormatter());
            }
        } while (append);

        try {
            new FileHandler("", true);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }
    }

    public void testFileHandler_3params() throws Exception {
        int limit = 120;
        int count = 1;
        handler = new FileHandler("%t/log/string", limit, count);
        assertEquals("character encoding is non equal to actual value",
                "iso-8859-1", handler.getEncoding());
        assertNotNull("Filter is null", handler.getFilter());
        assertNotNull("Formatter is null", handler.getFormatter());
        assertEquals("is non equal to actual value", Level.FINE, handler
                .getLevel());
        assertNotNull("ErrorManager is null", handler.getErrorManager());
        handler.publish(r);
        handler.close();
        // output 3 times, and all records left
        // append mode is true
        for (int i = 0; i < 3; i++) {
            handler = new FileHandler("%t/log/string", limit, count);
            handler.publish(r);
            handler.close();
        }
        assertFileContent(TEMPPATH + SEP + "log", "/string", new LogRecord[] {
                r, null, r, null, r, null, r }, new MockFormatter());

        try {
            new FileHandler("", limit, count);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }

        try {
            new FileHandler("%t/log/string", -1, count);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }

        try {
            new FileHandler("%t/log/string", limit, 0);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }
    }

    public void testFileHandler_4params() throws Exception {
        int limit = 120;
        int count = 1;
        boolean append = false;
        do {
            append = !append;
            handler = new FileHandler("%t/log/string", limit, count, append);
            assertEquals("character encoding is non equal to actual value",
                    "iso-8859-1", handler.getEncoding());
            assertNotNull("Filter is null", handler.getFilter());
            assertNotNull("Formatter is null", handler.getFormatter());
            assertEquals("is non equal to actual value", Level.FINE, handler
                    .getLevel());
            assertNotNull("ErrorManager is null", handler.getErrorManager());
            handler.publish(r);
            handler.close();
            // output 3 times, and all records left
            // append mode is true
            for (int i = 0; i < 3; i++) {
                handler = new FileHandler("%t/log/string", limit, count, append);
                handler.publish(r);
                handler.close();
            }
            if (append) {
                assertFileContent(TEMPPATH + SEP + "log", "/string",
                        new LogRecord[] { r, null, r, null, r, null, r },
                        new MockFormatter());
            } else {
                assertFileContent(TEMPPATH + SEP + "log", "/string",
                        new LogRecord[] { r }, new MockFormatter());
            }
        } while (append);

        try {
            new FileHandler("", limit, count, true);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }

        try {
            new FileHandler("%t/log/string", -1, count, false);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }

        try {
            new FileHandler("%t/log/string", limit, 0, true);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }
    }

    private void assertFileContent(String homepath, String filename,
            Formatter formatter) throws Exception {
        assertFileContent(homepath, filename, new LogRecord[] { r }, formatter);
    }

    private void assertFileContent(String homepath, String filename,
            LogRecord[] lr, Formatter formatter) throws Exception {
        handler.close();
        String msg = "";
        // if formatter is null, the file content should be empty
        // else the message should be formatted given records
        if (null != formatter) {
            StringBuffer sb = new StringBuffer();
            sb.append(formatter.getHead(handler));
            for (int i = 0; i < lr.length; i++) {
                if (null == lr[i] && i < lr.length - 1) {
                    // if one record is null and is not the last record, means
                    // here is
                    // output completion point, should output tail, then output
                    // head
                    // (ready for next output)
                    sb.append(formatter.getTail(handler));
                    sb.append(formatter.getHead(handler));
                } else {
                    sb.append(formatter.format(lr[i]));
                }
            }
            sb.append(formatter.getTail(handler));
            msg = sb.toString();
        }
        char[] chars = new char[msg.length()];
        Reader reader = null;
        try {
            reader = new BufferedReader(new FileReader(homepath + SEP
                    + filename));
            reader.read(chars);
            assertEquals(msg, new String(chars));
            // assert has reached the end of the file
            assertEquals(-1, reader.read());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e) {
                // don't care
            }
            reset(homepath, filename);
        }
    }

    /**
     * Does a cleanup of given file
     */
    private void reset(String homepath, String filename) {
        File file;
        try {
            file = new File(homepath + SEP + filename);
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    files[i].delete();
                }
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            file = new File(homepath + SEP + filename + ".lck");
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // This test fails on RI. Doesn't parse special pattern \"%t/%h."
    public void testInvalidParams() throws IOException {

        // %t and %p parsing can add file separator automatically

        // bad directory, IOException, append
        try {
            new FileHandler("%t/baddir/multi%g", true);
            fail("should throw IO exception");
        } catch (IOException e) {
        }
        File file = new File(TEMPPATH + SEP + "baddir" + SEP + "multi0");
        assertFalse(file.exists());
        try {
            new FileHandler("%t/baddir/multi%g", false);
            fail("should throw IO exception");
        } catch (IOException e) {
        }
        file = new File(TEMPPATH + SEP + "baddir" + SEP + "multi0");
        assertFalse(file.exists());

        try {
            new FileHandler("%t/baddir/multi%g", 12, 4);
            fail("should throw IO exception");
        } catch (IOException e) {
        }
        file = new File(TEMPPATH + SEP + "baddir" + SEP + "multi0");
        assertFalse(file.exists());

        try {
            new FileHandler("%t/java%u", -1, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testPublish() throws Exception {
        LogRecord[] r = new LogRecord[] { new LogRecord(Level.CONFIG, "msg__"),
                new LogRecord(Level.WARNING, "message"),
                new LogRecord(Level.INFO, "message for"),
                new LogRecord(Level.FINE, "message for test") };
        for (int i = 0; i < r.length; i++) {
            handler = new FileHandler("%t/log/stringPublish");
            handler.publish(r[i]);
            handler.close();
            assertFileContent(TEMPPATH + SEP + "log", "stringPublish",
                    new LogRecord[] { r[i] }, handler.getFormatter());
        }
    }

    public void testClose() throws Exception {
        FileHandler h = new FileHandler("%t/log/stringPublish");
        h.publish(r);
        h.close();
        assertFileContent(TEMPPATH + SEP + "log", "stringPublish", h
                .getFormatter());
    }

    /*
     * mock classes
     */
    public static class MockFilter implements Filter {
        public boolean isLoggable(LogRecord record) {
            return !record.getMessage().equals("false");
        }
    }

    public static class MockFormatter extends Formatter {
        public String format(LogRecord r) {
            if (null == r) {
                return "";
            }
            return r.getMessage() + " by MockFormatter\n";
        }

        public String getTail(Handler h) {
            return "tail\n";
        }

        public String getHead(Handler h) {
            return "head\n";
        }
    }
}
